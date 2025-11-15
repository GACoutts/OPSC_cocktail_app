package com.example.mixmate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Entity for caching cocktail data from external APIs for offline access
 * Stores cocktails fetched from API Ninjas and TheCocktailDB
 */
@Entity(tableName = "cocktail_cache")
@TypeConverters(Converters::class)
data class CocktailCacheEntity(
    @PrimaryKey
    val cocktailId: String,
    val name: String,
    val imageUrl: String?,
    val category: String,
    val rating: Double,
    val ingredients: List<String>,
    val instructions: String,
    val servings: String?,
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis()
)

/**
 * Extension to convert to SuggestedCocktail for UI display
 */
fun CocktailCacheEntity.toSuggestedCocktail(): com.example.mixmate.SuggestedCocktail {
    return com.example.mixmate.SuggestedCocktail(
        name = name,
        rating = rating,
        category = category,
        imageUrl = imageUrl,
        cocktailId = cocktailId,
        ingredients = ingredients
    )
}
