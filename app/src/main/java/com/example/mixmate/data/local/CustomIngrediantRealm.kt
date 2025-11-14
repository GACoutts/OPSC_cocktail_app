package com.example.mixmate.data.local

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class CustomIngredientRealm : RealmObject {
    var name: String = ""
    var amount: String = ""
    var unit: String = ""
}
