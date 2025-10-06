package com.example.mixmate.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomRecipeDao {
    
    @Query("SELECT * FROM custom_recipes ORDER BY createdAt DESC")
    fun getAllCustomRecipes(): Flow<List<CustomRecipeEntity>>
    
    @Query("SELECT * FROM custom_recipes WHERE id = :id")
    suspend fun getCustomRecipeById(id: Long): CustomRecipeEntity?
    
    @Query("SELECT * FROM custom_recipes WHERE name LIKE '%' || :searchQuery || '%' ORDER BY createdAt DESC")
    fun searchCustomRecipes(searchQuery: String): Flow<List<CustomRecipeEntity>>
    
    @Query("SELECT * FROM custom_recipes WHERE difficulty = :difficulty ORDER BY createdAt DESC")
    fun getRecipesByDifficulty(difficulty: String): Flow<List<CustomRecipeEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomRecipe(recipe: CustomRecipeEntity): Long
    
    @Update
    suspend fun updateCustomRecipe(recipe: CustomRecipeEntity)
    
    @Delete
    suspend fun deleteCustomRecipe(recipe: CustomRecipeEntity)
    
    @Query("DELETE FROM custom_recipes WHERE id = :id")
    suspend fun deleteCustomRecipeById(id: Long)
    
    @Query("SELECT COUNT(*) FROM custom_recipes")
    suspend fun getRecipeCount(): Int
    
    @Query("SELECT * FROM custom_recipes ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentRecipes(limit: Int): List<CustomRecipeEntity>
}