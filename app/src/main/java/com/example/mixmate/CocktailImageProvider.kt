package com.example.mixmate

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.min

// Data models (expanded to include drink name for fuzzy matching)
private data class CocktailDbResponse(
    @SerializedName("drinks") val drinks: List<CocktailDbDrink>?
)

private data class CocktailDbDrink(
    @SerializedName("strDrink") val name: String?,
    @SerializedName("strDrinkThumb") val thumb: String?
)

private interface TheCocktailDbService {
    @GET("search.php")
    suspend fun searchByName(@Query("s") name: String): CocktailDbResponse

    @GET("search.php")
    suspend fun searchByFirstLetter(@Query("f") letter: String): CocktailDbResponse
}

object CocktailImageProvider {
    private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/" // made internal for tests

    private fun createService(base: String): TheCocktailDbService = Retrofit.Builder()
        .baseUrl(base)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TheCocktailDbService::class.java)

    @Volatile private var service: TheCocktailDbService = createService(BASE_URL)

    // Test hook to replace backend with MockWebServer endpoint
    internal fun replaceServiceForTests(baseUrl: String) {
        service = createService(baseUrl)
        // Clear cache so previous lookups don't interfere with assertions
        cache.clear()
    }

    // Simple in-memory cache to avoid duplicate lookups across screens
    private val cache = mutableMapOf<String, String?>()

    private fun normalize(raw: String): String = raw.lowercase()
        .replace(Regex("\\(.*?\\)"), " ")          // remove parenthetical descriptors
        .replace("cocktail", " ")                    // drop the word cocktail
        .replace(Regex("[^a-z0-9 ]"), " ")           // non-alphanumeric -> space
        .replace(Regex("\\s+"), " ")                // collapse whitespace
        .trim()

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val rows = a.length + 1
        val cols = b.length + 1
        val dist = Array(rows) { IntArray(cols) }
        for (i in 0 until rows) dist[i][0] = i
        for (j in 0 until cols) dist[0][j] = j
        for (i in 1 until rows) {
            for (j in 1 until cols) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dist[i][j] = min(
                    min(dist[i - 1][j] + 1, dist[i][j - 1] + 1),
                    dist[i - 1][j - 1] + cost
                )
            }
        }
        return dist[a.length][b.length]
    }

    private suspend fun fetchImageUrlForName(name: String): String? {
        val key = name.lowercase().trim()
        cache[key]?.let { return it }
        val normalized = normalize(name)
        return try {
            service.searchByName(name).drinks?.firstOrNull()?.thumb?.let { url ->
                cache[key] = url
                SafeLog.d("CocktailImageProvider", "Direct match for '$name'")
                return url
            }
            // 2. Try simplified variants
            val variants = buildList {
                add(normalized)
                val tokens = normalized.split(' ').filter { it.isNotBlank() }
                if (tokens.size > 1) {
                    add(tokens.first())
                    add(tokens.last())
                    add(tokens.take(2).joinToString(" "))
                }
            }.distinct().filter { it.isNotBlank() }
            for (variant in variants) {
                val resp = service.searchByName(variant)
                val drinks = resp.drinks
                if (!drinks.isNullOrEmpty()) {
                    // If only one, use directly; otherwise choose smallest distance.
                    val best = if (drinks.size == 1) drinks.first() else {
                        var bestDrink: CocktailDbDrink? = null
                        var bestScore = Int.MAX_VALUE
                        for (d in drinks) {
                            val dn = d.name ?: continue
                            val score = levenshtein(normalized, normalize(dn))
                            if (score < bestScore) {
                                bestScore = score
                                bestDrink = d
                            }
                        }
                        bestDrink
                    }
                    val candidate = best?.thumb
                    if (candidate != null) {
                        cache[key] = candidate
                        SafeLog.d("CocktailImageProvider", "Variant '$variant' matched for '$name' via best-distance selection")
                        return candidate
                    }
                }
            }
            // 3. Fuzzy: first-letter search then pick smallest Levenshtein distance
            val firstLetter = normalized.firstOrNull()
            if (firstLetter != null) {
                val list = service.searchByFirstLetter(firstLetter.toString()).drinks.orEmpty()
                var bestUrl: String? = null
                var bestScore = Int.MAX_VALUE
                for (drink in list) {
                    val dName = drink.name ?: continue
                    val dnNorm = normalize(dName)
                    if (dnNorm.isBlank()) continue
                    val score = levenshtein(normalized, dnNorm)
                    if (score < bestScore) {
                        bestScore = score
                        bestUrl = drink.thumb
                    }
                }
                // Accept fuzzy match only if reasonably close
                val acceptThreshold = maxOf(2, normalized.length / 3)
                if (bestUrl != null && bestScore <= acceptThreshold) {
                    cache[key] = bestUrl
                    SafeLog.d("CocktailImageProvider", "Fuzzy match (score=$bestScore) accepted for '$name'")
                    return bestUrl
                } else {
                    SafeLog.d("CocktailImageProvider", "Fuzzy search found no acceptable match for '$name' (bestScore=$bestScore)")
                }
            }
            SafeLog.d("CocktailImageProvider", "No image found for '$name'")
            cache[key] = null
            null
        } catch (e: Exception) {
            SafeLog.w("CocktailImageProvider", "Image fetch failed for $name", e)
            cache[key] = null
            null
        }
    }

    // Enrich list with parallel lookups (bounded by coroutine dispatcher). Keeps existing resource placeholder if no URL.
    suspend fun enrichWithImages(items: List<SuggestedCocktail>): List<SuggestedCocktail> = coroutineScope {
        items.map { item ->
            async {
                val url = fetchImageUrlForName(item.name)
                if (url != null) item.copy(imageUrl = url) else item
            }
        }.awaitAll()
    }
}
