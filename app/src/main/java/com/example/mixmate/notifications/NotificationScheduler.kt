package com.example.mixmate.notifications

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Schedules daily notifications at 8 AM and 8 PM
 */
object NotificationScheduler {
    
    private const val MORNING_WORK_NAME = "morning_cocktail_fact"
    private const val EVENING_WORK_NAME = "evening_cocktail_fact"
    
    /**
     * Schedule daily notifications at 8 AM and 8 PM
     */
    fun scheduleDailyNotifications(context: Context) {
        // Cancel any existing work first
        WorkManager.getInstance(context).cancelUniqueWork(MORNING_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(EVENING_WORK_NAME)
        
        // Schedule 8 AM notification
        scheduleMorningNotification(context)
        
        // Schedule 8 PM notification
        scheduleEveningNotification(context)
    }
    
    /**
     * Cancel all scheduled notifications
     */
    fun cancelDailyNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(MORNING_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(EVENING_WORK_NAME)
    }
    
    private fun scheduleMorningNotification(context: Context) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If 8 AM has already passed today, schedule for tomorrow
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyCocktailNotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MORNING_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }
    
    private fun scheduleEveningNotification(context: Context) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM in 24-hour format
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If 8 PM has already passed today, schedule for tomorrow
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyCocktailNotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EVENING_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }
}

