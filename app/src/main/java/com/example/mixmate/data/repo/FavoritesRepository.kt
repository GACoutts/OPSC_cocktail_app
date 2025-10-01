package com.example.mixmate.data.repo

import com.example.mixmate.MixMateApp
import com.example.mixmate.data.local.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class FavoritesRepository {
    private val dao = MixMateApp.db.favoriteDao()

    fun getAll(): Flow<List<FavoriteEntity>> = dao.getAll()

    fun searchByName(query: String): Flow<List<FavoriteEntity>> =
        dao.searchByName(query)

    suspend fun getById(id: String): FavoriteEntity? = dao.getById(id)

    suspend fun upsert(entity: FavoriteEntity) = dao.upsert(entity)

    suspend fun deleteById(id: String) = dao.deleteById(id)
}