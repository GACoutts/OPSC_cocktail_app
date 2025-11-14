package com.example.mixmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "app_preferences";
    private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";
    private static final String KEY_RECIPE_UPDATES = "recipe_updates";
    private static final String KEY_MEASUREMENT_UNITS = "measurement_units";
    private static final String KEY_LANGUAGE = "selected_language";

    // UI Components
    private ImageButton btnBack;
    private MaterialCardView cardEditProfile, cardChangePassword, cardUnits, cardLanguage, cardPrivacy,
            cardHelpSupport, cardAbout, cardLogout;
    private MaterialSwitch switchPushNotifications, switchRecipeUpdates;
    private TextView tvCurrentUnits, tvCurrentLanguage;

    // Preferences
    private SharedPreferences sharedPreferences;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.INSTANCE.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initializeViews();
        setupClickListeners();
        loadPreferences();

        Log.d(TAG, "Settings activity initialized");
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
            cardLanguage = findViewById(R.id.card_language);
            tvCurrentLanguage = findViewById(R.id.tv_current_language);

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
            Toast.makeText(this, getString(R.string.toast_error_init_settings), Toast.LENGTH_SHORT).show();
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
            try {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting EditProfileActivity: " + e.getMessage());
                Toast.makeText(this, getString(R.string.toast_error_open_edit_profile), Toast.LENGTH_SHORT).show();
            }
        });

        cardChangePassword.setOnClickListener(v -> {
            Log.d(TAG, "Change Password clicked");
            Toast.makeText(this, getString(R.string.toast_change_password_coming), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to ChangePasswordActivity
        });

        // Preferences section
        cardUnits.setOnClickListener(v -> {
            Log.d(TAG, "Measurement Units clicked");
            showUnitsDialog();
        });

        cardLanguage.setOnClickListener(v -> {
            Log.d(TAG, "Language clicked");
            showLanguageDialog();
        });


        // Notifications section
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Push notifications switched: " + isChecked);
            saveBooleanPreference(KEY_PUSH_NOTIFICATIONS, isChecked);
            String message = isChecked ? getString(R.string.toast_push_enabled) : getString(R.string.toast_push_disabled);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        switchRecipeUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Recipe updates switched: " + isChecked);
            saveBooleanPreference(KEY_RECIPE_UPDATES, isChecked);
            String message = isChecked ? getString(R.string.toast_recipe_updates_enabled) : getString(R.string.toast_recipe_updates_disabled);
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

            // Load Notifications preferences
            boolean pushNotifications = sharedPreferences.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
            switchPushNotifications.setChecked(pushNotifications);

            boolean recipeUpdates = sharedPreferences.getBoolean(KEY_RECIPE_UPDATES, false);
            switchRecipeUpdates.setChecked(recipeUpdates);

            // Load Measurement Units preference
            String units = sharedPreferences.getString(KEY_MEASUREMENT_UNITS, getString(R.string.units_metric));
            tvCurrentUnits.setText(units);

            // Load Language preference
            String languageCode = LocaleHelper.INSTANCE.getLanguage(this);
            String languageName = languageCode.equals("af") ? getString(R.string.language_afrikaans) : getString(R.string.language_english);
            tvCurrentLanguage.setText(languageName);

            Log.d(TAG, "Preferences loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading preferences", e);
            Toast.makeText(this, getString(R.string.toast_error_loading_settings), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBooleanPreference(String key, boolean value) {
        try {
            sharedPreferences.edit().putBoolean(key, value).apply();
            Log.d(TAG, "Preference saved: " + key + " = " + value);
        } catch (Exception e) {
            Log.e(TAG, "Error saving preference: " + key, e);
            Toast.makeText(this, getString(R.string.toast_error_saving_setting), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveStringPreference(String key, String value) {
        try {
            sharedPreferences.edit().putString(key, value).apply();
            Log.d(TAG, "Preference saved: " + key + " = " + value);
        } catch (Exception e) {
            Log.e(TAG, "Error saving preference: " + key, e);
            Toast.makeText(this, getString(R.string.toast_error_saving_setting), Toast.LENGTH_SHORT).show();
        }
    }

    private void showUnitsDialog() {
        String[] units = {getString(R.string.units_metric), getString(R.string.units_imperial), getString(R.string.units_both)};
        String currentUnits = sharedPreferences.getString(KEY_MEASUREMENT_UNITS, getString(R.string.units_metric));

        int selectedIndex = 0;
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(currentUnits)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.units_dialog_title))
                .setSingleChoiceItems(units, selectedIndex, (dialog, which) -> {
                    String selectedUnits = units[which];
                    saveStringPreference(KEY_MEASUREMENT_UNITS, selectedUnits);
                    tvCurrentUnits.setText(selectedUnits);
                    Toast.makeText(this, getString(R.string.units_changed_to, selectedUnits), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.language_english), getString(R.string.language_afrikaans)};
        String[] languageCodes = {"en", "af"};

        String currentLanguage = LocaleHelper.INSTANCE.getLanguage(this);

        int selectedIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String selectedLanguageCode = languageCodes[which];
                    String selectedLanguageName = languages[which];

                    // Save language preference
                    LocaleHelper.INSTANCE.setLanguage(this, selectedLanguageCode);

                    // Update UI
                    tvCurrentLanguage.setText(selectedLanguageName);

                    Log.d(TAG, "Language changed to: " + selectedLanguageName);

                    // Recreate activity to apply language change
                    recreate();

                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
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
            Toast.makeText(this, getString(R.string.toast_privacy_policy_coming), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, getString(R.string.toast_no_email_app), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening help & support", e);
            Toast.makeText(this, getString(R.string.toast_help_support_coming), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.about_mixmate))
                .setMessage("MixMate v1.0.0\n\n" +
                           "The ultimate cocktail companion app.\n\n" +
                           "Discover, create, and share amazing cocktail recipes.\n\n" +
                           "© 2024 MixMate Team")
                .setPositiveButton(getString(R.string.dialog_ok), (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Rate App", (dialog, which) -> {
                    // In a real app, this would open the Play Store
                    Toast.makeText(this, getString(R.string.toast_thanks_rating_coming), Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_logout_title))
                .setMessage(getString(R.string.dialog_logout_message))
                .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        try {
            Log.d(TAG, "Starting logout process");
            
            // Sign out user from Firebase and clear local data using UserManager
            UserManager.INSTANCE.signOut(this);
            
            Log.d(TAG, "UserManager.signOut() completed");
            
            // Additional verification - check if user is still logged in
            boolean stillLoggedIn = UserManager.INSTANCE.isLoggedIn(this);
            Log.d(TAG, "User still logged in after signOut: " + stillLoggedIn);
            
            // Show logout message
            Toast.makeText(this, getString(R.string.toast_logged_out), Toast.LENGTH_SHORT).show();

            Log.d(TAG, "Navigating to MainActivity (login screen)");
            
            // Navigate to MainActivity (login screen) and clear the back stack
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            // Add extra flag to ensure complete task clearing
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Add extra data to indicate this is a logout scenario
            intent.putExtra("logout", true);
            
            startActivity(intent);
            finish();
            
            Log.d(TAG, "Logout process completed - should now be on login screen");
        } catch (Exception e) {
            Log.e(TAG, "Error during logout process", e);
            Toast.makeText(this, getString(R.string.toast_error_logout, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload preferences in case they were changed elsewhere
        loadPreferences();
    }
}
