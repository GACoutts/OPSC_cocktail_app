package com.example.mixmate.data.local

import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteDao(private val realm: Realm) {

    // GET ALL FAVORITES
    fun getAll(userId: String): Flow<List<FavoriteEntity>> {
        return realm.query<FavoriteEntity>(
            "userId == $0 SORT(savedAt DESC)", userId
        )
            .asFlow()
            .map { change: ResultsChange<FavoriteEntity> ->
                change.list.toList()
            }
    }

    // SEARCH BY NAME
    fun searchByName(userId: String, query: String): Flow<List<FavoriteEntity>> {
        return realm.query<FavoriteEntity>(
            "userId == $0 AND name CONTAINS[c] $1 SORT(savedAt DESC)",
            userId, query
        )
            .asFlow()
            .map { it.list.toList() }
    }

    // GET ONE BY ID
    suspend fun getById(id: String, userId: String): FavoriteEntity? {
        return realm.query<FavoriteEntity>(
            "cocktailId == $0 AND userId == $1", id, userId
        ).first().find()
    }

    // CHECK FAVORITE STATUS (REACTIVE)
    fun isFavoriteFlow(id: String, userId: String): Flow<Boolean> {
        return realm.query<FavoriteEntity>(
            "cocktailId == $0 AND userId == $1", id, userId
        )
            .asFlow()
            .map { it.list.isNotEmpty() }
    }

    // UPSERT (INSERT OR UPDATE)
    suspend fun upsert(entity: FavoriteEntity) {
        realm.write {
            copyToRealm(entity, updatePolicy = UpdatePolicy.ALL)
        }
    }

    // DELETE ENTITY
    suspend fun delete(entity: FavoriteEntity) {
        realm.write {
            val obj = query<FavoriteEntity>(
                "cocktailId == $0 AND userId == $1", entity.cocktailId, entity.userId
            ).first().find()
            if (obj != null) delete(obj)
        }
    }

    // DELETE BY ID
    suspend fun deleteById(id: String, userId: String) {
        realm.write {
            val obj = query<FavoriteEntity>(
                "cocktailId == $0 AND userId == $1", id, userId
            ).first().find()
            if (obj != null) delete(obj)
        }
    }
}
