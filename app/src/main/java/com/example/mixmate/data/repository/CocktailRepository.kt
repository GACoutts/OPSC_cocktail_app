package com.example.mixmate.data.repository

import android.content.Context
import android.util.Log
import com.example.mixmate.CocktailApiRepository
import com.example.mixmate.CocktailImageProvider
import com.example.mixmate.SuggestedCocktail
import com.example.mixmate.data.local.CocktailCacheDao
import com.example.mixmate.data.local.CocktailCacheEntity
import com.example.mixmate.data.local.toSuggestedCocktail
import com.example.mixmate.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository implementing offline-first strategy for cocktail data
 * - Loads from cache immediately for instant display
 * - Fetches from API in background when online
 * - Syncs cache with fresh data
 * - Falls back to cache when offline
 */
class CocktailRepository(
    private val context: Context,
    private val cacheDao: CocktailCacheDao
) {
    companion object {
        private const val TAG = "CocktailRepository"
        private const val CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    /**
     * Get cocktails with offline-first strategy
     * Returns cached data immediately, then updates with fresh data if online
     */
    fun getCocktails(limit: Int = 150, forceRefresh: Boolean = false): Flow<Result<List<SuggestedCocktail>>> = flow {
        try {
            // Check network status
            val isOnline = NetworkUtils.isNetworkAvailable(context)
            Log.d(TAG, "Getting cocktails - Online: $isOnline, ForceRefresh: $forceRefresh")

            // If offline, return cached data only
            if (!isOnline) {
                val cached = getCachedCocktails()
                if (cached.isNotEmpty()) {
                    Log.d(TAG, "Offline: Returning ${cached.size} cached cocktails")
                    emit(Result.success(cached))
                } else {
                    Log.w(TAG, "Offline: No cached data available")
                    emit(Result.failure(Exception("No internet connection and no cached data available")))
                }
                return@flow
            }

            // Online: check if cache is fresh and we don't need to force refresh
            if (!forceRefresh) {
                val cacheAge = getCacheAge()
                if (cacheAge != null && cacheAge < CACHE_VALIDITY_MS) {
                    val cached = getCachedCocktails()
                    if (cached.isNotEmpty()) {
                        Log.d(TAG, "Cache is fresh (${cacheAge}ms old), returning ${cached.size} cocktails")
                        emit(Result.success(cached))
                        return@flow
                    }
                }
            }

            // Fetch fresh data from API
            Log.d(TAG, "Fetching fresh data from API...")
            emit(Result.success(emptyList())) // Emit empty to show loading state

            val apiCocktails = withContext(Dispatchers.IO) {
                CocktailApiRepository.fetchCocktails(limit)
            }

            if (apiCocktails.isEmpty()) {
                Log.w(TAG, "API returned no cocktails, falling back to cache")
                val cached = getCachedCocktails()
                emit(Result.success(cached))
                return@flow
            }

            // Enrich with images
            Log.d(TAG, "Enriching ${apiCocktails.size} cocktails with images...")
            val enriched = withContext(Dispatchers.IO) {
                CocktailImageProvider.enrichWithImages(apiCocktails)
            }

            // Cache the fresh data
            Log.d(TAG, "Caching ${enriched.size} cocktails for offline access")
            cacheCocktails(enriched)

            // Emit the fresh data
            emit(Result.success(enriched))

            // Clean old cache entries (keep recent 200)
            withContext(Dispatchers.IO) {
                cacheDao.cleanOldCache()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching cocktails", e)

            // Try to return cached data as fallback
            val cached = getCachedCocktails()
            if (cached.isNotEmpty()) {
                Log.d(TAG, "Error occurred, returning ${cached.size} cached cocktails as fallback")
                emit(Result.success(cached))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    /**
     * Search cocktails with offline support
     */
    suspend fun searchCocktails(query: String): List<SuggestedCocktail> = withContext(Dispatchers.IO) {
        try {
            val isOnline = NetworkUtils.isNetworkAvailable(context)

            if (isOnline) {
                // Try to search in cached data first for instant results
                val cachedResults = cacheDao.searchCocktails(query)
                if (cachedResults.isNotEmpty()) {
                    return@withContext cachedResults.map { it.toSuggestedCocktail() }
                }
            }

            // Search in cache (works offline or online)
            cacheDao.searchCocktails(query).map { it.toSuggestedCocktail() }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching cocktails", e)
            emptyList()
        }
    }

    /**
     * Get cocktails by category with offline support
     */
    suspend fun getCocktailsByCategory(category: String): List<SuggestedCocktail> = withContext(Dispatchers.IO) {
        try {
            cacheDao.getCocktailsByCategory(category).map { it.toSuggestedCocktail() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cocktails by category", e)
            emptyList()
        }
    }

    /**
     * Get cocktails by minimum rating with offline support
     */
    suspend fun getCocktailsByMinRating(minRating: Double): List<SuggestedCocktail> = withContext(Dispatchers.IO) {
        try {
            cacheDao.getCocktailsByMinRating(minRating).map { it.toSuggestedCocktail() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cocktails by rating", e)
            emptyList()
        }
    }

    /**
     * Get cache statistics for UI display
     */
    suspend fun getCacheInfo(): CacheInfo = withContext(Dispatchers.IO) {
        try {
            val count = cacheDao.getCacheCount()
            val oldestTimestamp = cacheDao.getOldestCacheTimestamp()
            val age = oldestTimestamp?.let { System.currentTimeMillis() - it }

            CacheInfo(
                cocktailCount = count,
                cacheAgeMs = age,
                isStale = age?.let { it > CACHE_VALIDITY_MS } ?: true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cache info", e)
            CacheInfo(0, null, true)
        }
    }

    /**
     * Clear all cached cocktails
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        try {
            cacheDao.clearAll()
            Log.d(TAG, "Cache cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }

    /**
     * Manually sync cache with API (for pull-to-refresh)
     */
    suspend fun syncCache(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return@withContext Result.failure(Exception("No internet connection"))
            }

            Log.d(TAG, "Manual cache sync started")

            val apiCocktails = CocktailApiRepository.fetchCocktails(150)
            val enriched = CocktailImageProvider.enrichWithImages(apiCocktails)

            cacheCocktails(enriched)
            cacheDao.cleanOldCache()

            Log.d(TAG, "Cache synced with ${enriched.size} cocktails")
            Result.success(enriched.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing cache", e)
            Result.failure(e)
        }
    }

    // Private helper methods

    private suspend fun getCachedCocktails(): List<SuggestedCocktail> = withContext(Dispatchers.IO) {
        try {
            cacheDao.getAllCocktailsList().map { it.toSuggestedCocktail() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached cocktails", e)
            emptyList()
        }
    }

    private suspend fun cacheCocktails(cocktails: List<SuggestedCocktail>) = withContext(Dispatchers.IO) {
        try {
            val cacheEntities = cocktails.mapNotNull { cocktail ->
                // Only cache cocktails with valid IDs
                val id = cocktail.cocktailId ?: cocktail.name.hashCode().toString()

                CocktailCacheEntity(
                    cocktailId = id,
                    name = cocktail.name,
                    imageUrl = cocktail.imageUrl,
                    category = cocktail.category,
                    rating = cocktail.rating,
                    ingredients = cocktail.ingredients ?: emptyList(),
                    instructions = "", // Would need to fetch full details for this
                    servings = null,
                    cachedAt = System.currentTimeMillis(),
                    lastAccessedAt = System.currentTimeMillis()
                )
            }

            cacheDao.insertAll(cacheEntities)
            Log.d(TAG, "Cached ${cacheEntities.size} cocktails")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching cocktails", e)
        }
    }

    private suspend fun getCacheAge(): Long? = withContext(Dispatchers.IO) {
        try {
            val oldestTimestamp = cacheDao.getOldestCacheTimestamp()
            oldestTimestamp?.let { System.currentTimeMillis() - it }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Data class for cache information
     */
    data class CacheInfo(
        val cocktailCount: Int,
        val cacheAgeMs: Long?,
        val isStale: Boolean
    ) {
        val cacheAgeHours: Int?
            get() = cacheAgeMs?.let { (it / (1000 * 60 * 60)).toInt() }
    }
}
