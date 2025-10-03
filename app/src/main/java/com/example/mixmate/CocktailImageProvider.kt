package com.example.mixmate

import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// TheCocktailDB response models (only fields we need)
private data class CocktailDbResponse(
    @SerializedName("drinks") val drinks: List<CocktailDbDrink>?
)

private data class CocktailDbDrink(
    @SerializedName("strDrinkThumb") val thumb: String?
)

private interface TheCocktailDbService {
    @GET("search.php")
    suspend fun searchByName(@Query("s") name: String): CocktailDbResponse
}

object CocktailImageProvider {
    private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

    private val service: TheCocktailDbService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TheCocktailDbService::class.java)
    }

    // Simple in-memory cache to avoid duplicate lookups across screens
    private val cache = mutableMapOf<String, String?>()

    private suspend fun fetchImageUrlForName(name: String): String? {
        val key = name.lowercase().trim()
        cache[key]?.let { return it }
        return try {
            val resp = service.searchByName(name)
            val url = resp.drinks?.firstOrNull()?.thumb
            cache[key] = url
            url
        } catch (e: Exception) {
            Log.w("CocktailImageProvider", "Image fetch failed for $name", e)
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

