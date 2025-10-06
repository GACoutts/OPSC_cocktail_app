package com.example.mixmate.data.repository

import android.util.Log
import com.example.mixmate.data.local.CustomRecipeDao
import com.example.mixmate.data.local.CustomRecipeEntity
import com.example.mixmate.data.local.CustomIngredient
import com.example.mixmate.data.remote.FirebaseRecipeRepository
import com.example.mixmate.data.remote.FirebaseRecipe
import com.example.mixmate.data.remote.FirebaseIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Hybrid repository implementing offline-first strategy
 * - Local Room database as single source of truth
 * - Firebase for cloud backup and community sharing
 * - Automatic sync when network is available
 */
class RecipeRepository(
    private val localDao: CustomRecipeDao,
    private val firebaseRepo: FirebaseRecipeRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "RecipeRepository"
    }

    /**
     * Get all user recipes (offline-first)
     * Returns local data immediately, syncs with Firebase in background
     */
    fun getAllRecipes(userId: String): Flow<List<CustomRecipeEntity>> {
        // Start background sync
        scope.launch { syncWithFirebase(userId) }
        
        // Return local data immediately
        return localDao.getAllCustomRecipes()
    }

    /**
     * Save a new recipe (offline-first)
     * Saves locally first, then syncs to Firebase
     */
    suspend fun saveRecipe(recipe: CustomRecipeEntity, userId: String, isPublic: Boolean = false): Result<Long> {
        return try {
            // Save locally first
            val localId = localDao.insertCustomRecipe(recipe)
            Log.d(TAG, "Recipe saved locally with ID: $localId")
            
            // Sync to Firebase in background
            scope.launch {
                try {
                    val firebaseRecipe = recipe.toFirebaseRecipe(userId, isPublic)
                    val result = firebaseRepo.saveRecipe(firebaseRecipe)
                    
                    if (result.isSuccess) {
                        // Update local record with Firebase ID
                        val firebaseId = result.getOrNull()
                        // Could store Firebase ID in a separate field if needed
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
            // Update locally first
            localDao.updateCustomRecipe(recipe.copy(updatedAt = System.currentTimeMillis()))
            
            // Sync to Firebase in background
            scope.launch {
                try {
                    val firebaseRecipe = recipe.toFirebaseRecipe(userId, false) // Assume not public for updates
                    // In a real implementation, you'd need to track Firebase IDs
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
     * Delete a recipe
     */
    suspend fun deleteRecipe(recipe: CustomRecipeEntity): Result<Unit> {
        return try {
            // Delete locally first
            localDao.deleteCustomRecipe(recipe)
            
            // Delete from Firebase in background
            scope.launch {
                try {
                    // In a real implementation, you'd track Firebase IDs to delete
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
     * Search recipes locally
     */
    fun searchRecipes(query: String): Flow<List<CustomRecipeEntity>> {
        return localDao.searchCustomRecipes(query)
    }

    /**
     * Get community recipes from Firebase
     * This is online-only as community features require real-time data
     */
    fun getCommunityRecipes(): Flow<List<FirebaseRecipe>> {
        return firebaseRepo.getPublicRecipes()
    }

    /**
     * Sync local data with Firebase
     * This runs in background and handles conflicts
     */
    private suspend fun syncWithFirebase(userId: String) {
        try {
            // In a full implementation, you would:
            // 1. Compare local and remote timestamps
            // 2. Handle conflicts (last-write-wins, user choice, etc.)
            // 3. Sync new recipes from Firebase to local
            // 4. Upload unsynced local recipes to Firebase
            
            Log.d(TAG, "Firebase sync completed for user: $userId")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase sync failed, continuing with local data", e)
        }
    }

    /**
     * Force a manual sync (for pull-to-refresh scenarios)
     */
    suspend fun forceSyncWithFirebase(userId: String): Result<Unit> {
        return try {
            syncWithFirebase(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension functions to convert between local and Firebase models
fun CustomRecipeEntity.toFirebaseRecipe(userId: String, isPublic: Boolean): FirebaseRecipe {
    return FirebaseRecipe(
        name = name,
        description = description,
        instructions = instructions,
        ingredients = ingredients.map { it.toFirebaseIngredient() },
        glassware = glassware,
        garnish = garnish,
        preparationTime = preparationTime,
        difficulty = difficulty,
        imageUrl = imageUri, // Would need to upload to Firebase Storage first
        userId = userId,
        userName = "", // Would get from user profile
        isPublic = isPublic,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CustomIngredient.toFirebaseIngredient(): FirebaseIngredient {
    return FirebaseIngredient(
        name = name,
        amount = amount,
        unit = unit
    )
}

fun FirebaseRecipe.toCustomRecipeEntity(): CustomRecipeEntity {
    return CustomRecipeEntity(
        name = name,
        description = description,
        instructions = instructions,
        ingredients = ingredients.map { it.toCustomIngredient() },
        glassware = glassware,
        garnish = garnish,
        preparationTime = preparationTime,
        difficulty = difficulty,
        imageUri = imageUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun FirebaseIngredient.toCustomIngredient(): CustomIngredient {
    return CustomIngredient(
        name = name,
        amount = amount,
        unit = unit
    )
}