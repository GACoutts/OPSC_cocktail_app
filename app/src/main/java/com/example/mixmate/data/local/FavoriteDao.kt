package com.example.mixmate.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY savedAt DESC")
    fun getAll(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE userId = :userId AND name LIKE '%' || :query || '%' ORDER BY savedAt DESC")
    fun searchByName(userId: String, query: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE cocktailId = :id AND userId = :userId LIMIT 1")
    suspend fun getById(id: String, userId: String): FavoriteEntity?

    // handy if you ever want a reactive heart toggle state
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE cocktailId = :id AND userId = :userId)")
    fun isFavoriteFlow(id: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavoriteEntity)

    @Delete
    suspend fun delete(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE cocktailId = :id AND userId = :userId")
    suspend fun deleteById(id: String, userId: String)
}