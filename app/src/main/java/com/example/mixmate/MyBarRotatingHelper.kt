package com.example.mixmate

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class to manage rotating alcohol types for MyBar default display
 * Cycles through different alcohol types on each app launch
 */
object MyBarRotatingHelper {

    private const val PREFS_NAME = "MyBarPrefs"
    private const val KEY_LAUNCH_COUNT = "launch_count"

    // List of alcohol types to rotate through
    private val rotatingAlcoholTypes = listOf(
        "Vodka",
        "Gin",
        "Rum",
        "Tequila",
        "Whiskey",
        "Brandy"
    )

    /**
     * Get the current rotating alcohol type based on app launch count
     * Each launch cycles to the next alcohol type in the list
     */
    fun getRotatingAlcoholType(context: Context): String {
        val prefs = getPreferences(context)
        val launchCount = prefs.getInt(KEY_LAUNCH_COUNT, 0)

        // Increment launch count for next time
        prefs.edit().putInt(KEY_LAUNCH_COUNT, launchCount + 1).apply()

        // Rotate through alcohol types using modulo
        val index = launchCount % rotatingAlcoholTypes.size
        val selectedType = rotatingAlcoholTypes[index]

        android.util.Log.d("MyBarRotating", "Launch #$launchCount -> Showing: $selectedType")

        return selectedType
    }

    /**
     * Get the alcohol types list
     */
    fun getAlcoholTypesList(): List<String> = rotatingAlcoholTypes

    /**
     * Reset the launch counter (useful for testing)
     */
    fun resetLaunchCounter(context: Context) {
        getPreferences(context).edit().putInt(KEY_LAUNCH_COUNT, 0).apply()
    }

    /**
     * Get current launch count (for debugging)
     */
    fun getCurrentLaunchCount(context: Context): Int {
        return getPreferences(context).getInt(KEY_LAUNCH_COUNT, 0)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
