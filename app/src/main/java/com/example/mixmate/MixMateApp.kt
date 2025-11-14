package com.example.mixmate

// !!!! This runs for RoomDB purposes. this class starts up first to create the RoomDB for offline use !!!!!

import android.app.Application
import androidx.room.Room
import com.example.mixmate.data.local.AppDatabase
import com.example.mixmate.notifications.NotificationHelper

class MixMateApp : Application() {
    companion object {
        lateinit var db: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Apply saved language preference
        LocaleHelper.updateLocale(this, LocaleHelper.getLanguage(this))

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "mixmate.db"
        )
            .fallbackToDestructiveMigration()
            .build()
        println("MixMateApp started, DB created")

        // Initialize Firebase Auth listener for user state management
        UserManager.initializeAuthListener(this)

        // Create notification channel for push notifications
        NotificationHelper.createNotificationChannel(this)
    }
}