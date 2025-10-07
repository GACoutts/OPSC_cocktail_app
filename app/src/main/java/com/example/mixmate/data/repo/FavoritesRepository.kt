package com.example.mixmate.data.repo

import com.example.mixmate.MixMateApp
import com.example.mixmate.data.local.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class FavoritesRepository {
    private val dao = MixMateApp.db.favoriteDao()

    fun getAll(userId: String): Flow<List<FavoriteEntity>> = dao.getAll(userId)

    fun searchByName(userId: String, query: String): Flow<List<FavoriteEntity>> =
        dao.searchByName(userId, query)

    suspend fun getById(id: String, userId: String): FavoriteEntity? = dao.getById(id, userId)

    suspend fun upsert(entity: FavoriteEntity) = dao.upsert(entity)

    suspend fun deleteById(id: String, userId: String) = dao.deleteById(id, userId)
}
