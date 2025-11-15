package com.example.mixmate.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing cached cocktail data for offline mode
 */
@Dao
interface CocktailCacheDao {

    /**
     * Insert or replace a cached cocktail
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCocktail(cocktail: CocktailCacheEntity)

    /**
     * Insert multiple cocktails at once (for bulk caching)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cocktails: List<CocktailCacheEntity>)

    /**
     * Get all cached cocktails as Flow (reactive updates)
     */
    @Query("SELECT * FROM cocktail_cache ORDER BY lastAccessedAt DESC")
    fun getAllCocktails(): Flow<List<CocktailCacheEntity>>

    /**
     * Get all cached cocktails (one-time query)
     */
    @Query("SELECT * FROM cocktail_cache ORDER BY lastAccessedAt DESC")
    suspend fun getAllCocktailsList(): List<CocktailCacheEntity>

    /**
     * Get a specific cocktail by ID
     */
    @Query("SELECT * FROM cocktail_cache WHERE cocktailId = :cocktailId")
    suspend fun getCocktailById(cocktailId: String): CocktailCacheEntity?

    /**
     * Search cached cocktails by name or ingredient
     */
    @Query("SELECT * FROM cocktail_cache WHERE name LIKE '%' || :query || '%' OR ingredients LIKE '%' || :query || '%'")
    suspend fun searchCocktails(query: String): List<CocktailCacheEntity>

    /**
     * Filter by category
     */
    @Query("SELECT * FROM cocktail_cache WHERE category = :category ORDER BY rating DESC")
    suspend fun getCocktailsByCategory(category: String): List<CocktailCacheEntity>

    /**
     * Filter by minimum rating
     */
    @Query("SELECT * FROM cocktail_cache WHERE rating >= :minRating ORDER BY rating DESC")
    suspend fun getCocktailsByMinRating(minRating: Double): List<CocktailCacheEntity>

    /**
     * Update last accessed time (for LRU cache management)
     */
    @Query("UPDATE cocktail_cache SET lastAccessedAt = :timestamp WHERE cocktailId = :cocktailId")
    suspend fun updateLastAccessed(cocktailId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Get count of cached cocktails
     */
    @Query("SELECT COUNT(*) FROM cocktail_cache")
    suspend fun getCacheCount(): Int

    /**
     * Delete old cached entries (keep most recent 200)
     * Useful for managing cache size
     */
    @Query("DELETE FROM cocktail_cache WHERE cocktailId NOT IN (SELECT cocktailId FROM cocktail_cache ORDER BY lastAccessedAt DESC LIMIT 200)")
    suspend fun cleanOldCache()

    /**
     * Delete a specific cocktail from cache
     */
    @Delete
    suspend fun deleteCocktail(cocktail: CocktailCacheEntity)

    /**
     * Clear all cached cocktails
     */
    @Query("DELETE FROM cocktail_cache")
    suspend fun clearAll()

    /**
     * Get cache age in milliseconds for the oldest entry
     */
    @Query("SELECT MIN(cachedAt) FROM cocktail_cache")
    suspend fun getOldestCacheTimestamp(): Long?
}
