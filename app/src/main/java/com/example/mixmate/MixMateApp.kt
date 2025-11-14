package com.example.mixmate

import android.app.Application
import com.example.mixmate.data.local.CustomIngredientRealm
import com.example.mixmate.data.local.CustomRecipeDao
import com.example.mixmate.data.local.CustomRecipeEntity
import com.example.mixmate.data.local.FavoriteDao
import com.example.mixmate.data.local.FavoriteEntity
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.TypedRealmObject
import kotlin.reflect.KClass

class MixMateApp : Application() {

    companion object {
        lateinit var realm: Realm
            private set

        lateinit var customRecipeDao: CustomRecipeDao
            private set

        lateinit var favoriteDao: FavoriteDao
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Configure Realm
        val config = RealmConfiguration.Builder(
            schema = setOf(
                FavoriteEntity::class,
                CustomRecipeEntity::class,
                CustomIngredientRealm::class
            ) as Set<KClass<out TypedRealmObject>>
        )
            .schemaVersion(1)
            .name("mixmate.realm")
            .build()

        // Open Realm ONCE for the entire app
        realm = Realm.open(config)

        customRecipeDao = CustomRecipeDao(realm)
        favoriteDao = FavoriteDao(realm)

        println("MixMateApp started, Realm initialized")

        // Firebase user state listener (unchanged)
        UserManager.initializeAuthListener(this)
    }
}
