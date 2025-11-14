package com.example.mixmate.data.local

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.TypedRealmObject
import kotlin.reflect.KClass

object RealmProvider {

    private var realmInstance: Realm? = null

    fun getRealm(): Realm {
        if (realmInstance == null) {
            val config = RealmConfiguration.Builder(
                schema = setOf(
                    FavoriteEntity::class,
                    CustomRecipeEntity::class,
                    CustomIngredientRealm::class
                ) as Set<KClass<out TypedRealmObject>>
            )
                .name("mixmate.realm")
                .schemaVersion(1)
                .build()

            realmInstance = Realm.open(config)
        }
        return realmInstance!!
    }
}
