package com.example.mixmate

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * Simple user data management using SharedPreferences
 * This is a temporary solution until proper authentication is implemented
 */
object UserManager {
    
    private const val PREFS_NAME = "mixmate_user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_SURNAME = "user_surname"
    private const val KEY_USERNAME = "username"
    private const val KEY_JOIN_DATE = "join_date"
    private const val KEY_PROFILE_PICTURE_URI = "profile_picture_uri"
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Save user data after successful signup/login
     */
    fun saveUserData(
        context: Context,
        name: String,
        surname: String,
        username: String
    ) {
        val prefs = getSharedPreferences(context)
        val currentDate = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        
        with(prefs.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_SURNAME, surname)
            putString(KEY_USERNAME, username)
            putString(KEY_JOIN_DATE, "Joined $currentDate")
            apply()
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Get user's display name (name + surname)
     */
    fun getDisplayName(context: Context): String {
        val prefs = getSharedPreferences(context)
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val surname = prefs.getString(KEY_USER_SURNAME, "") ?: ""
        
        return if (name.isNotEmpty() && surname.isNotEmpty()) {
            "$name $surname"
        } else if (name.isNotEmpty()) {
            name
        } else {
            "MixMate User"
        }
    }
    
    /**
     * Get username with @ prefix
     */
    fun getUsername(context: Context): String {
        val prefs = getSharedPreferences(context)
        val username = prefs.getString(KEY_USERNAME, "") ?: ""
        
        return if (username.isNotEmpty()) {
            "@${username.lowercase()}"
        } else {
            "@mixmate"
        }
    }
    
    /**
     * Get join date
     */
    fun getJoinDate(context: Context): String {
        val prefs = getSharedPreferences(context)
        return prefs.getString(KEY_JOIN_DATE, "Joined 2025") ?: "Joined 2025"
    }
    
    /**
     * Save profile picture URI
     */
    fun saveProfilePictureUri(context: Context, uri: String) {
        val prefs = getSharedPreferences(context)
        with(prefs.edit()) {
            putString(KEY_PROFILE_PICTURE_URI, uri)
            apply()
        }
    }
    
    /**
     * Get profile picture URI
     */
    fun getProfilePictureUri(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_PROFILE_PICTURE_URI, null)
    }
    
    /**
     * Clear all user data (logout)
     */
    fun clearUserData(context: Context) {
        val prefs = getSharedPreferences(context)
        with(prefs.edit()) {
            clear()
            apply()
        }
    }
}