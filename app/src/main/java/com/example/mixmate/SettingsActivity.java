package com.example.mixmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "app_preferences";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";
    private static final String KEY_RECIPE_UPDATES = "recipe_updates";
    private static final String KEY_MEASUREMENT_UNITS = "measurement_units";

    // UI Components
    private ImageButton btnBack;
    private MaterialCardView cardEditProfile, cardChangePassword, cardUnits, cardPrivacy,
            cardHelpSupport, cardAbout, cardLogout;
    private MaterialSwitch switchDarkMode, switchPushNotifications, switchRecipeUpdates;
    private TextView tvCurrentUnits;

    // Preferences
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply dark theme before setting content view
        applyTheme();
        
        setContentView(R.layout.activity_settings);

        // Initialize preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initializeViews();
        setupClickListeners();
        loadPreferences();

        Log.d(TAG, "Settings activity initialized");
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, true); // Default to dark mode
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void initializeViews() {
        try {
            // Navigation
            btnBack = findViewById(R.id.btn_back);

            // Account section
            cardEditProfile = findViewById(R.id.card_edit_profile);
            cardChangePassword = findViewById(R.id.card_change_password);

            // Preferences section
            cardUnits = findViewById(R.id.card_units);
            tvCurrentUnits = findViewById(R.id.tv_current_units);
            switchDarkMode = findViewById(R.id.switch_dark_mode);

            // Notifications section
            switchPushNotifications = findViewById(R.id.switch_push_notifications);
            switchRecipeUpdates = findViewById(R.id.switch_recipe_updates);

            // Privacy & Support section
            cardPrivacy = findViewById(R.id.card_privacy);
            cardHelpSupport = findViewById(R.id.card_help_support);
            cardAbout = findViewById(R.id.card_about);

            // Account actions
            cardLogout = findViewById(R.id.card_logout);

            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error initializing settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        // Account section
        cardEditProfile.setOnClickListener(v -> {
            Log.d(TAG, "Edit Profile clicked");
            Toast.makeText(this, "Edit Profile - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to EditProfileActivity
        });

        cardChangePassword.setOnClickListener(v -> {
            Log.d(TAG, "Change Password clicked");
            Toast.makeText(this, "Change Password - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to ChangePasswordActivity
        });

        // Preferences section
        cardUnits.setOnClickListener(v -> {
            Log.d(TAG, "Measurement Units clicked");
            showUnitsDialog();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Dark mode switched: " + isChecked);
            saveBooleanPreference(KEY_DARK_MODE, isChecked);
            
            // Show restart dialog for theme change
            showThemeChangeDialog();
        });

        // Notifications section
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Push notifications switched: " + isChecked);
            saveBooleanPreference(KEY_PUSH_NOTIFICATIONS, isChecked);
            String message = isChecked ? "Push notifications enabled" : "Push notifications disabled";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        switchRecipeUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Recipe updates switched: " + isChecked);
            saveBooleanPreference(KEY_RECIPE_UPDATES, isChecked);
            String message = isChecked ? "Recipe updates enabled" : "Recipe updates disabled";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Privacy & Support section
        cardPrivacy.setOnClickListener(v -> {
            Log.d(TAG, "Privacy Policy clicked");
            openPrivacyPolicy();
        });

        cardHelpSupport.setOnClickListener(v -> {
            Log.d(TAG, "Help & Support clicked");
            openHelpSupport();
        });

        cardAbout.setOnClickListener(v -> {
            Log.d(TAG, "About clicked");
            showAboutDialog();
        });

        // Account actions
        cardLogout.setOnClickListener(v -> {
            Log.d(TAG, "Logout clicked");
            showLogoutDialog();

        });
    }

    private void loadPreferences() {
        try {
            // Load Dark Mode preference
            boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, true);
            switchDarkMode.setChecked(isDarkMode);

            // Load Notifications preferences
            boolean pushNotifications = sharedPreferences.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
            switchPushNotifications.setChecked(pushNotifications);

            boolean recipeUpdates = sharedPreferences.getBoolean(KEY_RECIPE_UPDATES, false);
            switchRecipeUpdates.setChecked(recipeUpdates);

            // Load Measurement Units preference
            String units = sharedPreferences.getString(KEY_MEASUREMENT_UNITS, "Metric (ml, cl)");
            tvCurrentUnits.setText(units);

            Log.d(TAG, "Preferences loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading preferences", e);
            Toast.makeText(this, "Error loading settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBooleanPreference(String key, boolean value) {
        try {
            sharedPreferences.edit().putBoolean(key, value).apply();
            Log.d(TAG, "Preference saved: " + key + " = " + value);
        } catch (Exception e) {
            Log.e(TAG, "Error saving preference: " + key, e);
            Toast.makeText(this, "Error saving setting", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveStringPreference(String key, String value) {
        try {
            sharedPreferences.edit().putString(key, value).apply();
            Log.d(TAG, "Preference saved: " + key + " = " + value);
        } catch (Exception e) {
            Log.e(TAG, "Error saving preference: " + key, e);
            Toast.makeText(this, "Error saving setting", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUnitsDialog() {
        String[] units = {"Metric (ml, cl)", "Imperial (fl oz, cups)", "Both"};
        String currentUnits = sharedPreferences.getString(KEY_MEASUREMENT_UNITS, "Metric (ml, cl)");
        
        int selectedIndex = 0;
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(currentUnits)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Measurement Units")
                .setSingleChoiceItems(units, selectedIndex, (dialog, which) -> {
                    String selectedUnits = units[which];
                    saveStringPreference(KEY_MEASUREMENT_UNITS, selectedUnits);
                    tvCurrentUnits.setText(selectedUnits);
                    Toast.makeText(this, "Units changed to " + selectedUnits, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showThemeChangeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Theme Changed")
                .setMessage("The app needs to restart to apply the theme change. Restart now?")
                .setPositiveButton("Restart", (dialog, which) -> {
                    // Restart the app by going to DiscoverPage
                    Intent intent = new Intent(this, DiscoverPage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openPrivacyPolicy() {
        try {
            // In a real app, this would be your actual privacy policy URL
            String url = "https://example.com/privacy-policy";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening privacy policy", e);
            Toast.makeText(this, "Privacy Policy - Coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openHelpSupport() {
        try {
            // Create email intent for support
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@mixmate.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MixMate Support Request");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi MixMate Team,\n\nI need help with:\n\n");
            
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening help & support", e);
            Toast.makeText(this, "Help & Support - Coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About MixMate")
                .setMessage("MixMate v1.0.0\n\n" +
                           "The ultimate cocktail companion app.\n\n" +
                           "Discover, create, and share amazing cocktail recipes.\n\n" +
                           "© 2024 MixMate Team")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Rate App", (dialog, which) -> {
                    // In a real app, this would open the Play Store
                    Toast.makeText(this, "Thank you! Rating coming soon.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        try {
            Log.d(TAG, "Performing logout");
            
            // Clear user data using UserManager
            UserManager.INSTANCE.clearUserData(this);
            
            // Show logout message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Navigate to MainActivity and clear the back stack
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
            Log.d(TAG, "Logout completed");
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload preferences in case they were changed elsewhere
        loadPreferences();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed");
        super.onBackPressed();
    }
}