package com.example.mixmate.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class FirebaseRecipe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val instructions: String = "",
    val ingredients: List<FirebaseIngredient> = emptyList(),
    val glassware: String? = null,
    val garnish: String? = null,
    val preparationTime: Int? = null,
    val difficulty: String? = null,
    val imageUrl: String? = null, // Firebase Storage URL
    val userId: String = "", // Owner of the recipe
    val userName: String = "", // Display name of creator
    val isPublic: Boolean = false, // Whether recipe is shared publicly
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val likedBy: List<String> = emptyList() // User IDs who liked this recipe
)

data class FirebaseIngredient(
    val name: String = "",
    val amount: String = "",
    val unit: String = ""
)

class FirebaseRecipeRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val recipesCollection = firestore.collection("recipes")
    
    companion object {
        private const val TAG = "FirebaseRecipeRepo"
    }

    /**
     * Save a new recipe to Firebase
     */
    suspend fun saveRecipe(recipe: FirebaseRecipe): Result<String> {
        return try {
            val docRef = recipesCollection.add(recipe).await()
            Log.d(TAG, "Recipe saved with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving recipe", e)
            Result.failure(e)
        }
    }

    /**
     * Update an existing recipe
     */
    suspend fun updateRecipe(recipeId: String, recipe: FirebaseRecipe): Result<Unit> {
        return try {
            val updatedRecipe = recipe.copy(
                id = recipeId,
                updatedAt = System.currentTimeMillis()
            )
            recipesCollection.document(recipeId).set(updatedRecipe).await()
            Log.d(TAG, "Recipe updated: $recipeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating recipe", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a recipe
     */
    suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            recipesCollection.document(recipeId).delete().await()
            Log.d(TAG, "Recipe deleted: $recipeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting recipe", e)
            Result.failure(e)
        }
    }

    /**
     * Get user's own recipes (real-time updates)
     */
    fun getUserRecipes(userId: String): Flow<List<FirebaseRecipe>> = callbackFlow {
        val listener = recipesCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user recipes", error)
                    return@addSnapshotListener
                }
                
                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(recipes)
            }
        
        awaitClose { listener.remove() }
    }

    /**
     * Get public recipes (community recipes)
     */
    fun getPublicRecipes(): Flow<List<FirebaseRecipe>> = callbackFlow {
        val listener = recipesCollection
            .whereEqualTo("isPublic", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to public recipes", error)
                    return@addSnapshotListener
                }
                
                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(recipes)
            }
        
        awaitClose { listener.remove() }
    }

    /**
     * Search recipes by name
     */
    suspend fun searchRecipes(query: String, includePublic: Boolean = true): Result<List<FirebaseRecipe>> {
        return try {
            val queryBuilder = recipesCollection
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
            
            if (includePublic) {
                queryBuilder.whereEqualTo("isPublic", true)
            }
            
            val snapshot = queryBuilder.get().await()
            val recipes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
            }
            
            Result.success(recipes)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching recipes", e)
            Result.failure(e)
        }
    }

    /**
     * Like/unlike a recipe
     */
    suspend fun toggleLike(recipeId: String, userId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val docRef = recipesCollection.document(recipeId)
                val snapshot = transaction.get(docRef)
                val recipe = snapshot.toObject(FirebaseRecipe::class.java) ?: return@runTransaction
                
                val currentLikes = recipe.likedBy.toMutableList()
                val newLikes: List<String>
                val newLikeCount: Int
                
                if (currentLikes.contains(userId)) {
                    // Unlike
                    currentLikes.remove(userId)
                    newLikes = currentLikes
                    newLikeCount = recipe.likes - 1
                } else {
                    // Like
                    currentLikes.add(userId)
                    newLikes = currentLikes
                    newLikeCount = recipe.likes + 1
                }
                
                transaction.update(docRef, mapOf(
                    "likes" to newLikeCount,
                    "likedBy" to newLikes
                ))
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like", e)
            Result.failure(e)
        }
    }

    /**
     * Get a single recipe by ID
     */
    suspend fun getRecipeById(recipeId: String): Result<FirebaseRecipe?> {
        return try {
            val snapshot = recipesCollection.document(recipeId).get().await()
            val recipe = snapshot.toObject(FirebaseRecipe::class.java)?.copy(id = snapshot.id)
            Result.success(recipe)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipe by ID", e)
            Result.failure(e)
        }
    }
}