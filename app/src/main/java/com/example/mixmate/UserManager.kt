package com.example.mixmate

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import java.text.SimpleDateFormat
import java.util.*

/**
 * User data management integrating Firebase Authentication with local storage
 * Handles both Firebase Auth tokens and local user preferences
 */
object UserManager {
    
    private const val PREFS_NAME = "mixmate_user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_SURNAME = "user_surname"
    private const val KEY_USERNAME = "username"
    private const val KEY_JOIN_DATE = "join_date"
    private const val KEY_PROFILE_PICTURE_URI = "profile_picture_uri"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_UID = "user_uid"
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Initialize Firebase Auth state listener to sync authentication state with SharedPreferences
     */
    fun initializeAuthListener(context: Context) {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in, sync Firebase user data to SharedPreferences
                syncFirebaseUserToPrefs(context, user)
            } else {
                // User is signed out, clear local user data
                clearUserData(context)
            }
        }
    }
    
    /**
     * Sync Firebase user data to SharedPreferences for local access
     */
    private fun syncFirebaseUserToPrefs(context: Context, user: FirebaseUser) {
        val prefs = getPrefs(context)
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_UID, user.uid)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.displayName?.substringBefore(" ") ?: "")
            putString(KEY_USER_SURNAME, user.displayName?.substringAfter(" ", "") ?: "")
            putString(KEY_USERNAME, user.displayName ?: user.email?.substringBefore("@") ?: "")
            putString(KEY_PROFILE_PICTURE_URI, user.photoUrl?.toString() ?: "")
            // Set join date to current time if not previously set
            if (!prefs.contains(KEY_JOIN_DATE)) {
                putString(KEY_JOIN_DATE, SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date()))
            }
            apply()
        }
    }
    
    /**
     * Save user data after successful signup/login and update Firebase profile
     */
    fun saveUserData(
        context: Context,
        name: String,
        surname: String,
        username: String
    ) {
        val prefs = getPrefs(context)
        val currentDate = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        
        with(prefs.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_SURNAME, surname)
            putString(KEY_USERNAME, username)
            putString(KEY_JOIN_DATE, "Joined $currentDate")
            apply()
        }
        
        // Update Firebase profile with display name
        auth.currentUser?.let { user ->
            val displayName = "$name $surname"
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            
            user.updateProfile(profileUpdates)
        }
    }
    
    /**
     * Check if user is logged in by verifying both Firebase Auth and SharedPreferences
     */
    fun isLoggedIn(context: Context): Boolean {
        val firebaseLoggedIn = auth.currentUser != null
        val prefsLoggedIn = getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
        
        // Sync state if they're out of sync
        if (firebaseLoggedIn != prefsLoggedIn) {
            if (firebaseLoggedIn && auth.currentUser != null) {
                syncFirebaseUserToPrefs(context, auth.currentUser!!)
            } else {
                clearUserData(context)
            }
        }
        
        return firebaseLoggedIn
    }
    
    /**
     * Get user's display name (name + surname)
     */
    fun getDisplayName(context: Context): String {
        val prefs = getPrefs(context)
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
        val prefs = getPrefs(context)
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
        val prefs = getPrefs(context)
        return prefs.getString(KEY_JOIN_DATE, "Joined 2025") ?: "Joined 2025"
    }
    
    /**
     * Save profile picture URI
     */
    fun saveProfilePictureUri(context: Context, uri: String) {
        val prefs = getPrefs(context)
        with(prefs.edit()) {
            putString(KEY_PROFILE_PICTURE_URI, uri)
            apply()
        }
    }
    
    /**
     * Get profile picture URI
     */
    fun getProfilePictureUri(context: Context): String? {
        return getPrefs(context).getString(KEY_PROFILE_PICTURE_URI, null)
    }
    
    /**
     * Get current Firebase user UID
     */
    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Get current user email
     */
    fun getCurrentUserEmail(context: Context): String? {
        return auth.currentUser?.email ?: getPrefs(context).getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Sign out user from Firebase and clear local data
     */
    fun signOut(context: Context) {
        auth.signOut()
        clearUserData(context)
    }
    
    /**
     * Clear all user data (logout)
     */
    fun clearUserData(context: Context) {
        val prefs = getPrefs(context)
        with(prefs.edit()) {
            clear()
            apply()
        }
    }
}