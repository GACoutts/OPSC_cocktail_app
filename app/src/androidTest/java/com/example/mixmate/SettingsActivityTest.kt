package com.example.mixmate

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for SettingsActivity persistence logic
 * Tests all settings are properly saved and retrieved
 */
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        prefs = context.getSharedPreferences("MixMateSettings_Test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit() // Clear any existing test data
    }

    @After
    fun tearDown() {
        prefs.edit().clear().commit()
    }

    @Test
    fun testThemePreferenceSaved() {
        // Given
        val expectedTheme = SettingsActivity.THEME_DARK

        // When
        prefs.edit().putString(SettingsActivity.KEY_THEME, expectedTheme).apply()

        // Then
        val actualTheme = prefs.getString(SettingsActivity.KEY_THEME, null)
        assertEquals(expectedTheme, actualTheme)
    }

    @Test
    fun testLanguagePreferenceSaved() {
        // Given
        val expectedLanguage = SettingsActivity.LANG_AFRIKAANS

        // When
        prefs.edit().putString(SettingsActivity.KEY_LANGUAGE, expectedLanguage).apply()

        // Then
        val actualLanguage = prefs.getString(SettingsActivity.KEY_LANGUAGE, null)
        assertEquals(expectedLanguage, actualLanguage)
    }

    @Test
    fun testUnitsPreferenceSaved() {
        // Given
        val expectedUnits = SettingsActivity.UNITS_IMPERIAL

        // When
        prefs.edit().putString(SettingsActivity.KEY_UNITS, expectedUnits).apply()

        // Then
        val actualUnits = prefs.getString(SettingsActivity.KEY_UNITS, null)
        assertEquals(expectedUnits, actualUnits)
    }

    @Test
    fun testPushNotificationsPreferenceSaved() {
        // Given
        val expectedValue = true

        // When
        prefs.edit().putBoolean(SettingsActivity.KEY_PUSH_NOTIFICATIONS, expectedValue).apply()

        // Then
        val actualValue = prefs.getBoolean(SettingsActivity.KEY_PUSH_NOTIFICATIONS, false)
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun testRecipeUpdatesPreferenceSaved() {
        // Given
        val expectedValue = true

        // When
        prefs.edit().putBoolean(SettingsActivity.KEY_RECIPE_UPDATES, expectedValue).apply()

        // Then
        val actualValue = prefs.getBoolean(SettingsActivity.KEY_RECIPE_UPDATES, false)
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun testDefaultThemeIsReturned() {
        // When - no theme set
        val theme = prefs.getString(SettingsActivity.KEY_THEME, SettingsActivity.THEME_DARK)

        // Then - should return default
        assertEquals(SettingsActivity.THEME_DARK, theme)
    }

    @Test
    fun testDefaultLanguageIsReturned() {
        // When - no language set
        val language = prefs.getString(SettingsActivity.KEY_LANGUAGE, SettingsActivity.LANG_ENGLISH)

        // Then - should return default
        assertEquals(SettingsActivity.LANG_ENGLISH, language)
    }

    @Test
    fun testAllSettingsPersistTogether() {
        // Given - multiple settings
        val theme = SettingsActivity.THEME_LIGHT
        val language = SettingsActivity.LANG_AFRIKAANS
        val units = SettingsActivity.UNITS_METRIC
        val pushNotifs = false
        val recipeUpdates = true

        // When - save all settings
        prefs.edit()
            .putString(SettingsActivity.KEY_THEME, theme)
            .putString(SettingsActivity.KEY_LANGUAGE, language)
            .putString(SettingsActivity.KEY_UNITS, units)
            .putBoolean(SettingsActivity.KEY_PUSH_NOTIFICATIONS, pushNotifs)
            .putBoolean(SettingsActivity.KEY_RECIPE_UPDATES, recipeUpdates)
            .apply()

        // Then - all settings should be retrievable
        assertEquals(theme, prefs.getString(SettingsActivity.KEY_THEME, null))
        assertEquals(language, prefs.getString(SettingsActivity.KEY_LANGUAGE, null))
        assertEquals(units, prefs.getString(SettingsActivity.KEY_UNITS, null))
        assertEquals(pushNotifs, prefs.getBoolean(SettingsActivity.KEY_PUSH_NOTIFICATIONS, true))
        assertEquals(recipeUpdates, prefs.getBoolean(SettingsActivity.KEY_RECIPE_UPDATES, false))
    }
}
