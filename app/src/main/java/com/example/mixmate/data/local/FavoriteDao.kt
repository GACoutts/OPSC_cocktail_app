package com.example.mixmate.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun getAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE name LIKE '%' || :query || '%' ORDER BY savedAt DESC")
    fun searchByName(query: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE cocktailId = :id LIMIT 1")
    suspend fun getById(id: String): FavoriteEntity?

    // handy if you ever want a reactive heart toggle state
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE cocktailId = :id)")
    fun isFavoriteFlow(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavoriteEntity)

    @Delete
    suspend fun delete(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE cocktailId = :id")
    suspend fun deleteById(id: String)
}