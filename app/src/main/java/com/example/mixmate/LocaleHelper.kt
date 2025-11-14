package com.example.mixmate

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "selected_language"
    private const val PREFS_NAME = "app_preferences"

    /**
     * Set the app language and update the configuration
     */
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
        updateLocale(context, languageCode)
    }

    /**
     * Get the currently selected language
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }

    /**
     * Update the app's locale configuration
     */
    fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    /**
     * Apply the saved language on app start
     */
    fun onAttach(context: Context): Context {
        val language = getLanguage(context)
        return updateResources(context, language)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
}

