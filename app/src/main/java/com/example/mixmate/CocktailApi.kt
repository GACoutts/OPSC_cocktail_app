package com.example.mixmate

import com.example.mixmate.BuildConfig.API_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.random.Random

// Data model returned by API Ninjas cocktail endpoint
// Example JSON element:
// {
//   "name": "Margarita",
//   "ingredients": ["1 1/2 oz Tequila", "1 oz Triple sec", ...],
//   "instructions": "Rub the rim...",
//   "servings": 1
// }

data class ApiCocktail(
    val name: String?,
    val ingredients: List<String>?,
    val instructions: String?,
    val servings: Int?
)

interface CocktailApiService {
    @GET("v1/cocktail")
    suspend fun searchCocktails(@Query("name") name: String): List<ApiCocktail>
}

private class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val newReq = original.newBuilder()
            .addHeader("X-Api-Key", API_KEY)
            .build()
        return chain.proceed(newReq)
    }
}

object CocktailApiRepository {
    private const val DEFAULT_BASE_URL = "https://api.api-ninjas.com/"

    // Allow tests to override base URL / service and skip API key requirement.
    @Volatile private var baseUrl: String = DEFAULT_BASE_URL
    @Volatile private var allowBlankApiKeyForTests: Boolean = false

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor())
            .build()
    }

    @Volatile private var service: CocktailApiService = buildService()

    private fun buildService(): CocktailApiService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(CocktailApiService::class.java)

    // Internal hooks for androidTest/unit tests
    internal fun replaceBaseUrlForTests(newBase: String) {
        baseUrl = newBase
        service = buildService()
    }
    internal fun enableBlankApiKeyForTests() { allowBlankApiKeyForTests = true }

    // Public suspend function to get up to [limit] cocktails. Uses a broad letter query to widen results.
    suspend fun fetchCocktails(limit: Int = 10): List<SuggestedCocktail> = withContext(Dispatchers.IO) {
        if (!allowBlankApiKeyForTests && API_KEY.isBlank()) {
            SafeLog.w("CocktailApi", "API_KEY is blank; skipping network call")
            return@withContext emptyList()
        }
        return@withContext try {
            NetworkIdlingResource.increment()
            val apiItems = service.searchCocktails("a")
            apiItems.asSequence()
                .filter { !it.name.isNullOrBlank() }
                .take(limit)
                .map { api ->
                    val baseSpirit = extractBaseSpirit(api.ingredients)
                    SuggestedCocktail(
                        name = api.name!!.trim(),
                        rating = randomRating(),
                        category = baseSpirit,
                        imageRes = R.drawable.cosmopolitan // Placeholder image
                    )
                }
                .toList()
        } catch (e: Exception) {
            SafeLog.w("CocktailApi", "Error fetching cocktails", e)
            emptyList()
        } finally {
            NetworkIdlingResource.decrement()
        }
    }

    private fun randomRating(): Double {
        // Generate a rating between 3.5 and 5.0 rounded to one decimal.
        val value = Random.nextDouble(3.5, 5.0)
        return (value * 10).toInt() / 10.0
    }

    private val spirits = listOf("Vodka", "Rum", "Tequila", "Whiskey", "Gin", "Brandy")

    private fun extractBaseSpirit(ingredients: List<String>?): String {
        if (ingredients.isNullOrEmpty()) return "General"
        val joined = ingredients.joinToString(" ").lowercase()
        return spirits.firstOrNull { joined.contains(it.lowercase()) } ?: "General"
    }
}
