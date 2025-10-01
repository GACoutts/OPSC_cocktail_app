package com.example.mixmate

// !!!! This runs for RoomDB purposes. this class starts up first to create the RoomDB for offline use !!!!!

import android.app.Application
import androidx.room.Room
import com.example.mixmate.data.local.AppDatabase

class MixMateApp : Application() {
    companion object {
        lateinit var db: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "mixmate.db"
        )
            .fallbackToDestructiveMigration()
            .build()
        println("MixMateApp started, DB created")
    }
}