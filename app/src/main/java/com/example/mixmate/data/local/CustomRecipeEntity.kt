package com.example.mixmate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "custom_recipes")
@TypeConverters(Converters::class)
data class CustomRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val instructions: String,
    val ingredients: List<CustomIngredient>,
    val glassware: String? = null,
    val garnish: String? = null,
    val preparationTime: Int? = null, // in minutes
    val difficulty: String? = null,
    val imageUri: String? = null, // Local file path or URI
    val userId: String, // Firebase user ID to associate recipe with user
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CustomIngredient(
    val name: String,
    val amount: String,
    val unit: String
)

class Converters {
    @TypeConverter
    fun fromIngredientsList(ingredients: List<CustomIngredient>): String {
        return Gson().toJson(ingredients)
    }

    @TypeConverter
    fun toIngredientsList(ingredientsString: String): List<CustomIngredient> {
        val listType = object : TypeToken<List<CustomIngredient>>() {}.type
        return Gson().fromJson(ingredientsString, listType)
    }

    @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return Gson().toJson(strings)
    }

    @TypeConverter
    fun toStringList(stringsString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(stringsString, listType)
    }
}
