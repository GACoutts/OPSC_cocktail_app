package com.example.mixmate.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomRecipeDao {
    
    @Query("SELECT * FROM custom_recipes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllCustomRecipes(userId: String): Flow<List<CustomRecipeEntity>>
    
    @Query("SELECT * FROM custom_recipes WHERE id = :id AND userId = :userId")
    suspend fun getCustomRecipeById(id: Long, userId: String): CustomRecipeEntity?
    
    @Query("SELECT * FROM custom_recipes WHERE userId = :userId AND name LIKE '%' || :searchQuery || '%' ORDER BY createdAt DESC")
    fun searchCustomRecipes(userId: String, searchQuery: String): Flow<List<CustomRecipeEntity>>
    
    @Query("SELECT * FROM custom_recipes WHERE userId = :userId AND difficulty = :difficulty ORDER BY createdAt DESC")
    fun getRecipesByDifficulty(userId: String, difficulty: String): Flow<List<CustomRecipeEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomRecipe(recipe: CustomRecipeEntity): Long
    
    @Update
    suspend fun updateCustomRecipe(recipe: CustomRecipeEntity)
    
    @Delete
    suspend fun deleteCustomRecipe(recipe: CustomRecipeEntity)
    
    @Query("DELETE FROM custom_recipes WHERE id = :id AND userId = :userId")
    suspend fun deleteCustomRecipeById(id: Long, userId: String)
    
    @Query("SELECT COUNT(*) FROM custom_recipes WHERE userId = :userId")
    suspend fun getRecipeCount(userId: String): Int
    
    @Query("SELECT * FROM custom_recipes WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentRecipes(userId: String, limit: Int): List<CustomRecipeEntity>
}