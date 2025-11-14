package com.example.mixmate.data.local

import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CustomRecipeDao(private val realm: Realm) {

    // GET ALL RECIPES
    fun getAllCustomRecipes(userId: String): Flow<List<CustomRecipeEntity>> {
        return realm.query<CustomRecipeEntity>(
            "userId == $0 SORT(createdAt DESC)", userId
        )
            .asFlow()
            .map { change: ResultsChange<CustomRecipeEntity> ->
                change.list.toList()
            }
    }

    // GET ONE BY ID
    suspend fun getCustomRecipeById(id: Long, userId: String): CustomRecipeEntity? {
        return realm.query<CustomRecipeEntity>(
            "id == $0 AND userId == $1", id, userId
        ).first().find()
    }

    // SEARCH BY NAME
    fun searchCustomRecipes(userId: String, searchQuery: String): Flow<List<CustomRecipeEntity>> {
        return realm.query<CustomRecipeEntity>(
            "userId == $0 AND name CONTAINS[c] $1 SORT(createdAt DESC)",
            userId, searchQuery
        )
            .asFlow()
            .map { it.list.toList() }
    }

    // GET BY DIFFICULTY
    fun getRecipesByDifficulty(userId: String, difficulty: String): Flow<List<CustomRecipeEntity>> {
        return realm.query<CustomRecipeEntity>(
            "userId == $0 AND difficulty == $1 SORT(createdAt DESC)",
            userId, difficulty
        )
            .asFlow()
            .map { it.list.toList() }
    }

    // INSERT
    suspend fun insertCustomRecipe(recipe: CustomRecipeEntity): Long {
        return realm.write {
            // Auto-generate ID if 0
            if (recipe.id == 0L) {
                val maxId = query<CustomRecipeEntity>().max("id").find()?.toLong() ?: 0L
                recipe.id = maxId + 1
            }
            copyToRealm(recipe, updatePolicy = UpdatePolicy.ALL)
            recipe.id
        }
    }

    // UPDATE
    suspend fun updateCustomRecipe(recipe: Long) {
        realm.write {
            copyToRealm(recipe, updatePolicy = UpdatePolicy.ALL)
        }
    }

    // DELETE
    suspend fun deleteCustomRecipe(recipe: CustomRecipeEntity) {
        realm.write {
            val obj = query<CustomRecipeEntity>(
                "id == $0 AND userId == $1", recipe.id, recipe.userId
            ).first().find()
            if (obj != null) delete(obj)
        }
    }

    // DELETE BY ID
    suspend fun deleteCustomRecipeById(id: Long, userId: String) {
        realm.write {
            val obj = query<CustomRecipeEntity>(
                "id == $0 AND userId == $1", id, userId
            ).first().find()
            if (obj != null) delete(obj)
        }
    }

    // COUNT
    suspend fun getRecipeCount(userId: String): Int {
        return realm.query<CustomRecipeEntity>("userId == $0", userId)
            .count()
            .find()
            .toInt()
    }

    //RECENTS
    suspend fun getRecentRecipes(userId: String, limit: Int): List<CustomRecipeEntity> {
        return realm.query<CustomRecipeEntity>(
            "userId == $0 SORT(createdAt DESC)", userId
        )
            .limit(limit)
            .find()
    }
}
