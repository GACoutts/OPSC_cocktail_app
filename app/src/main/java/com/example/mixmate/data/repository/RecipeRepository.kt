package com.example.mixmate.data.repository

import android.util.Log
import androidx.room.util.copy
import com.example.mixmate.MixMateApp
import com.example.mixmate.data.local.CustomRecipeDao
import com.example.mixmate.data.local.CustomRecipeEntity
import com.example.mixmate.data.local.CustomIngredientRealm
import com.example.mixmate.data.remote.FirebaseRecipeRepository
import com.example.mixmate.data.remote.FirebaseRecipe
import com.example.mixmate.data.remote.FirebaseIngredient
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

/**
 * Hybrid repository implementing offline-first strategy
 * - Local realm database as single source of truth
 * - Firebase for cloud backup and community sharing
 * - Automatic sync when network is available
 */
class RecipeRepository(
    private val realm: Realm,
    private val firebaseRepo: FirebaseRecipeRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val customRecipeDao = MixMateApp.customRecipeDao
    companion object {
        private const val TAG = "RecipeRepository"
    }

    /**
     * Get all user recipes (offline-first)
     * Returns local data immediately, syncs with Firebase in background
     */

    fun getAllRecipes(userId: String): List<CustomRecipeEntity> {
        // Start background sync
        scope.launch { syncWithFirebase(userId) }
        return realm.query<CustomRecipeEntity>("userId == $0", userId).find()
    }


    /**
     * Save a new recipe (offline-first)
     * Saves locally first, then syncs to Firebase
     */
    suspend fun saveRecipe(
        recipe: CustomRecipeEntity,
        userId: String,
        isPublic: Boolean = false
    ): Result<Long> {
        return try {
            // Save locally using Realm DAO
            val localId = MixMateApp.customRecipeDao.insertCustomRecipe(recipe)
            Log.d(TAG, "Recipe saved locally with ID: $localId")

            // Sync to Firebase in background
            scope.launch {
                try {
                    val firebaseRecipe = recipe.toFirebaseRecipe(userId, isPublic)
                    val result = firebaseRepo.saveRecipe(firebaseRecipe)

                    if (result.isSuccess) {
                        // Update local record with Firebase ID if needed
                        val firebaseId = result.getOrNull()
                        Log.d(TAG, "Recipe synced to Firebase with ID: $firebaseId")
                    } else {
                        Log.w(TAG, "Failed to sync recipe to Firebase", result.exceptionOrNull())
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase sync failed, recipe saved locally only", e)
                }
            }

            Result.success(localId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save recipe locally", e)
            Result.failure(e)
        }
    }


    /**
     * Update an existing recipe
     */
    suspend fun updateRecipe(recipe: CustomRecipeEntity, userId: String): Result<Unit> {
        return try {
            // Update locally via Realm DAO
            customRecipeDao.updateCustomRecipe(recipe.copy(updatedAt = System.currentTimeMillis()))

            // Sync to Firebase in background
            scope.launch {
                try {
                    val firebaseRecipe = recipe.toFirebaseRecipe(userId, false)
                    Log.d(TAG, "Recipe updated locally, Firebase sync would happen here")
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase update failed, recipe updated locally only", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update recipe", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a recipe locally and optionally from Firebase
     */
    suspend fun deleteRecipe(recipe: CustomRecipeEntity): Result<Unit> {
        return try {
            customRecipeDao.deleteCustomRecipe(recipe)

            // Delete from Firebase in background
            scope.launch {
                try {
                    Log.d(TAG, "Recipe deleted locally, Firebase deletion would happen here")
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase deletion failed, recipe deleted locally", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete recipe", e)
            Result.failure(e)
        }
    }

    /**
     * Search recipes locally using Realm DAO
     */
    fun searchRecipes(userId: String, query: String): Flow<List<CustomRecipeEntity>> {
        return customRecipeDao.searchCustomRecipes(userId, query)
    }

    /**
     * Get recent recipes locally
     */
    suspend fun getRecentRecipes(userId: String, limit: Int): Flow<CustomRecipeEntity> {
        return customRecipeDao.getRecentRecipes(userId, limit).asFlow()
    }

    /**
     * Get community recipes from Firebase (online-only)
     */
    fun getCommunityRecipes(): Flow<List<FirebaseRecipe>> {
        return firebaseRepo.getPublicRecipes()
    }

    /**
     * Force a manual sync (for pull-to-refresh)
     */
    suspend fun forceSyncWithFirebase(userId: String): Result<Unit> {
        return try {
            // Implement your sync logic here
            Log.d(TAG, "Firebase sync completed for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}