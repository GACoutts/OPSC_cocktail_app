package com.example.mixmate.data.local

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class FavoriteEntity : RealmObject {

    @PrimaryKey
    var cocktailId: String = ""  // same as Room

    var name: String = ""
    var imageUrl: String = ""
    var ingredients: String = ""
    var instructions: String = ""
    var userId: String = ""
    var savedAt: Long = System.currentTimeMillis()
}
