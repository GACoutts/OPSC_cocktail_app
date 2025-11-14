package com.example.mixmate

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mixmate.notifications.NotificationHelper
import com.example.mixmate.notifications.NotificationScheduler
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.util.Log

/**
 * Settings Activity - Handles all app settings per POE Part 3 requirements:
 * - Theme preferences (dark/light/system)
 * - Language selection (multi-language support)
 * - Notification preferences
 * - Account management (sign out, delete account)
 * - Privacy policy & support links
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var btnBack: ImageButton
    
    // Account Section
    private lateinit var cardEditProfile: MaterialCardView
    private lateinit var cardChangePassword: MaterialCardView
    
    // Preferences Section
    private lateinit var cardTheme: MaterialCardView
    private lateinit var tvCurrentTheme: TextView
    private lateinit var cardLanguage: MaterialCardView
    private lateinit var tvCurrentLanguage: TextView
    private lateinit var cardUnits: MaterialCardView
    private lateinit var tvCurrentUnits: TextView
    
    // Notifications Section
    private lateinit var switchPushNotifications: MaterialSwitch
    private lateinit var switchRecipeUpdates: MaterialSwitch
    private lateinit var switchDailyFacts: MaterialSwitch

    // Privacy & Support Section
    private lateinit var cardPrivacy: MaterialCardView
    private lateinit var cardHelpSupport: MaterialCardView
    private lateinit var cardAbout: MaterialCardView
    
    // Account Actions
    private lateinit var cardLogout: MaterialCardView

    companion object {
        private const val PREFS_NAME = "MixMateSettings"
        const val KEY_THEME = "theme_mode"
        const val KEY_LANGUAGE = "app_language"
        const val KEY_UNITS = "measurement_units"
        const val KEY_PUSH_NOTIFICATIONS = "push_notifications"
        const val KEY_RECIPE_UPDATES = "recipe_updates"
        const val KEY_DAILY_FACTS = "daily_cocktail_facts"

        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

        // Theme modes
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_SYSTEM = "system"
        
        // Languages (POE requirement: 2+ South African languages)
        const val LANG_ENGLISH = "en"
        const val LANG_AFRIKAANS = "af"
        const val LANG_ZULU = "zu"
        
        // Units
        const val UNITS_METRIC = "metric"
        const val UNITS_IMPERIAL = "imperial"
    }

    // Permission launcher for notifications (Android 13+)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, create channel then enable daily facts
            NotificationHelper.createNotificationChannel(this)
            enableDailyFacts()
        } else {
            // Permission denied, disable switch
            switchDailyFacts.isChecked = false
            Toast.makeText(
                this,
                "Notification permission is required for daily cocktail facts",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val requestGenericNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (pendingEnablePush && !isGranted) {
            switchPushNotifications.isChecked = false
            Toast.makeText(this, "Notification permission required", Toast.LENGTH_SHORT).show()
        } else if (pendingEnableRecipe && !isGranted) {
            switchRecipeUpdates.isChecked = false
            Toast.makeText(this, "Notification permission required", Toast.LENGTH_SHORT).show()
        } else if (isGranted) {
            // Create channel once permission granted
            NotificationHelper.createNotificationChannel(this)
            if (pendingEnablePush) {
                prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, true).apply()
                Toast.makeText(this, "Push notifications enabled", Toast.LENGTH_SHORT).show()
            }
            if (pendingEnableRecipe) {
                prefs.edit().putBoolean(KEY_RECIPE_UPDATES, true).apply()
                Toast.makeText(this, "Recipe updates enabled", Toast.LENGTH_SHORT).show()
            }
        }
        pendingEnablePush = false
        pendingEnableRecipe = false
    }

    private var pendingEnablePush = false
    private var pendingEnableRecipe = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        initializeViews()
        loadCurrentSettings()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btn_back)
        
        // Account
        cardEditProfile = findViewById(R.id.card_edit_profile)
        cardChangePassword = findViewById(R.id.card_change_password)
        
        // Preferences
        cardTheme = findViewById(R.id.card_theme)
        tvCurrentTheme = findViewById(R.id.tv_current_theme)
        cardLanguage = findViewById(R.id.card_language)
        tvCurrentLanguage = findViewById(R.id.tv_current_language)
        cardUnits = findViewById(R.id.card_units)
        tvCurrentUnits = findViewById(R.id.tv_current_units)
        
        // Notifications
        switchPushNotifications = findViewById(R.id.switch_push_notifications)
        switchRecipeUpdates = findViewById(R.id.switch_recipe_updates)
        switchDailyFacts = findViewById(R.id.switch_daily_facts)

        // Privacy & Support
        cardPrivacy = findViewById(R.id.card_privacy)
        cardHelpSupport = findViewById(R.id.card_help_support)
        cardAbout = findViewById(R.id.card_about)
        
        // Account Actions
        cardLogout = findViewById(R.id.card_logout)
    }

    private fun loadCurrentSettings() {
        // Load theme
        val currentTheme = prefs.getString(KEY_THEME, THEME_DARK) ?: THEME_DARK
        tvCurrentTheme.text = when (currentTheme) {
            THEME_LIGHT -> "Light"
            THEME_DARK -> "Dark"
            THEME_SYSTEM -> "Follow System"
            else -> "Dark"
        }
        
        // Load language
        // Use LocaleHelper as the source of truth for language preference so the app-wide
        // locale and Settings view remain in sync even if other parts of the app write
        // to language preferences.
        val currentLanguage = LocaleHelper.getLanguage(this)
        // Keep a mirrored copy in this activity prefs for any other legacy code that
        // reads from `MixMateSettings`.
        prefs.edit().putString(KEY_LANGUAGE, currentLanguage).apply()
        tvCurrentLanguage.text = when (currentLanguage) {
            LANG_ENGLISH -> "English"
            LANG_AFRIKAANS -> "Afrikaans"
            LANG_ZULU -> "isiZulu"
            else -> "English"
        }
        
        // Load units
        val currentUnits = prefs.getString(KEY_UNITS, UNITS_METRIC) ?: UNITS_METRIC
        tvCurrentUnits.text = when (currentUnits) {
            UNITS_METRIC -> "Metric (ml, cl)"
            UNITS_IMPERIAL -> "Imperial (oz, fl oz)"
            else -> "Metric (ml, cl)"
        }
        
        // Load notification preferences
        switchPushNotifications.isChecked = prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true)
        switchRecipeUpdates.isChecked = prefs.getBoolean(KEY_RECIPE_UPDATES, false)
        switchDailyFacts.isChecked = prefs.getBoolean(KEY_DAILY_FACTS, false)
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }
        
        // Account Section
        cardEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        
        cardChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        
        // Preferences Section
        cardTheme.setOnClickListener {
            showThemeDialog()
        }
        
        cardLanguage.setOnClickListener {
            showLanguageDialog()
        }
        
        cardUnits.setOnClickListener {
            showUnitsDialog()
        }
        
        // Notifications
        switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    pendingEnablePush = true
                    requestGenericNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    NotificationHelper.createNotificationChannel(this)
                    prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, true).apply()
                    Toast.makeText(this, "Push notifications enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, false).apply()
                Toast.makeText(this, "Push notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }

        switchRecipeUpdates.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    pendingEnableRecipe = true
                    requestGenericNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    NotificationHelper.createNotificationChannel(this)
                    prefs.edit().putBoolean(KEY_RECIPE_UPDATES, true).apply()
                    Toast.makeText(this, "Recipe updates enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                prefs.edit().putBoolean(KEY_RECIPE_UPDATES, false).apply()
                Toast.makeText(this, "Recipe updates disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Daily Cocktail Facts - Schedule notifications at 8 AM and 8 PM
        switchDailyFacts.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            NotificationHelper.createNotificationChannel(this)
                            enableDailyFacts()
                        }
                        shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                            // Show explanation and request permission
                            MaterialAlertDialogBuilder(this)
                                .setTitle(getString(R.string.notification_permission_rationale_title))
                                .setMessage(getString(R.string.notification_permission_rationale_message))
                                .setPositiveButton(getString(R.string.notification_permission_enable)) { _, _ ->
                                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                .setNegativeButton(getString(R.string.notification_permission_not_now)) { _, _ ->
                                    switchDailyFacts.isChecked = false
                                }
                                .show()
                        }
                        else -> {
                            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    // Android 12 and below don't need runtime permission
                    enableDailyFacts()
                }
            } else {
                disableDailyFacts()
            }
        }

        // Privacy & Support
        cardPrivacy.setOnClickListener {
            showPrivacyPolicyDialog()
        }
        
        cardHelpSupport.setOnClickListener {
            showHelpSupportDialog()
        }
        
        cardAbout.setOnClickListener {
            showAboutDialog()
        }
        
        // Account Actions
        cardLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    // Theme Dialog
    private fun showThemeDialog() {
        val themes = arrayOf("Dark", "Light", "Follow System")
        val currentTheme = prefs.getString(KEY_THEME, THEME_DARK) ?: THEME_DARK
        val currentIndex = when (currentTheme) {
            THEME_DARK -> 0
            THEME_LIGHT -> 1
            THEME_SYSTEM -> 2
            else -> 0
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("App Theme")
            .setSingleChoiceItems(themes, currentIndex) { dialog, which ->
                val selectedTheme = when (which) {
                    0 -> THEME_DARK
                    1 -> THEME_LIGHT
                    2 -> THEME_SYSTEM
                    else -> THEME_DARK
                }
                
                prefs.edit().putString(KEY_THEME, selectedTheme).apply()
                applyTheme(selectedTheme)
                tvCurrentTheme.text = themes[which]
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    // Language Dialog (POE Part 3 requirement)
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Afrikaans", "isiZulu")
        val currentLanguage = prefs.getString(KEY_LANGUAGE, LANG_ENGLISH) ?: LANG_ENGLISH
        val currentIndex = when (currentLanguage) {
            LANG_ENGLISH -> 0
            LANG_AFRIKAANS -> 1
            LANG_ZULU -> 2
            else -> 0
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Language")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = when (which) {
                    0 -> LANG_ENGLISH
                    1 -> LANG_AFRIKAANS
                    2 -> LANG_ZULU
                    else -> LANG_ENGLISH
                }

                // Save to this activity prefs (legacy) and to the LocaleHelper prefs which
                // are used by attachBaseContext to apply the locale app-wide.
                prefs.edit().putString(KEY_LANGUAGE, selectedLanguage).apply()
                LocaleHelper.setLanguage(this, selectedLanguage)
                // Update UI immediately and recreate activity to apply new locale
                tvCurrentLanguage.text = languages[which]
                Toast.makeText(this, "Language updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                // Recreate activity so resources are reloaded with the new locale
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Units Dialog
    private fun showUnitsDialog() {
        val units = arrayOf("Metric (ml, cl)", "Imperial (oz, fl oz)")
        val currentUnits = prefs.getString(KEY_UNITS, UNITS_METRIC) ?: UNITS_METRIC
        val currentIndex = if (currentUnits == UNITS_METRIC) 0 else 1
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Measurement Units")
            .setSingleChoiceItems(units, currentIndex) { dialog, which ->
                val selectedUnits = if (which == 0) UNITS_METRIC else UNITS_IMPERIAL
                prefs.edit().putString(KEY_UNITS, selectedUnits).apply()
                tvCurrentUnits.text = units[which]
                Toast.makeText(this, "Units updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Change Password Dialog
    private fun showChangePasswordDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Change Password")
            .setMessage("This feature requires Firebase Authentication. You'll receive an email to reset your password.")
            .setPositiveButton("Send Email") { _, _ ->
                sendPasswordResetEmail()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordResetEmail() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: UserManager.getCurrentUserEmail(this)
        
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "No email found for this account", Toast.LENGTH_SHORT).show()
            return
        }
        
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Password reset email sent to $email",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                Log.e("SettingsActivity", "Failed to send password reset email", e)
                Toast.makeText(
                    this,
                    "Failed to send email: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Privacy Policy Dialog
    private fun showPrivacyPolicyDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Privacy Policy")
            .setMessage(
                "MixMate Privacy Policy\n\n" +
                "We respect your privacy. This app collects:\n\n" +
                "• Account information (email, username)\n" +
                "• Custom recipes you create\n" +
                "• Your favorite cocktails\n\n" +
                "Your data is stored securely in Firebase and is not shared with third parties.\n\n" +
                "For more information, visit our website."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // Help & Support Dialog
    private fun showHelpSupportDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Help & Support")
            .setMessage(
                "Need help with MixMate?\n\n" +
                "Email: support@mixmate.app\n" +
                "GitHub: github.com/mixmate/support\n\n" +
                "We typically respond within 24 hours."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // About Dialog
    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About MixMate")
            .setMessage(
                "MixMate v1.0.0\n\n" +
                "Your ultimate cocktail companion.\n\n" +
                "Developed by:\n" +
                "Teejay Kamwaro – ST10274142\n" +
                "Grant Coutts - ST10258297\n" +
                "Tristan Vries - ST10380906\n" +
                "Kelvin Gravett - ST10108660\n\n" +
                "© 2025 MixMate. All rights reserved."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // Logout Confirmation
    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun enableDailyFacts() {
        prefs.edit().putBoolean(KEY_DAILY_FACTS, true).apply()
        NotificationScheduler.scheduleDailyNotifications(this)
        Toast.makeText(
            this,
            "Daily cocktail facts enabled! You'll receive notifications at 8 AM and 8 PM 🍹",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun disableDailyFacts() {
        prefs.edit().putBoolean(KEY_DAILY_FACTS, false).apply()
        NotificationScheduler.cancelDailyNotifications(this)
        Toast.makeText(
            this,
            "Daily cocktail facts disabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun performLogout() {
        try {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()
            
            // Clear user data from UserManager
            UserManager.clearUserData(this)
            
            // Clear app-specific preferences if needed
            prefs.edit()
                .putBoolean(KEY_PUSH_NOTIFICATIONS, false)
                .putBoolean(KEY_RECIPE_UPDATES, false)
                .apply()
            
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            
            // Navigate back to login
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("logout", true)
            }
            startActivity(intent)
            finish()
            
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Error during logout", e)
            Toast.makeText(this, "Error logging out: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
