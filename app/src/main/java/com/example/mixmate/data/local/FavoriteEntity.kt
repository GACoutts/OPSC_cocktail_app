package com.example.mixmate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val cocktailId: String,   // TheCocktailDB id
    val name: String,
    val imageUrl: String,
    val ingredients: String,              // multi-line text  saved for offline
    val instructions: String,
    val savedAt: Long = System.currentTimeMillis()
)
