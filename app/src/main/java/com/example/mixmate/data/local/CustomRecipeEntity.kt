package com.example.mixmate.data.local

import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.realm.kotlin.ext.realmListOf

class CustomRecipeEntity : RealmObject {

    @PrimaryKey
    var id: Long = 0

    var name: String = ""
    var description: String = ""
    var instructions: String = ""

    // RealmList instead of Gson type converter
    var ingredients: RealmList<CustomIngredientRealm> = realmListOf()

    var glassware: String? = null
    var garnish: String? = null
    var preparationTime: Int? = null
    var difficulty: String? = null
    var imageUri: String? = null

    var userId: String = ""

    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()
}
