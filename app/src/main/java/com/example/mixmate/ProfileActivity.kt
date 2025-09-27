package com.example.mixmate

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ProfileActivity : AppCompatActivity() {

    // Header views
    private lateinit var btnBack: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var imgProfile: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvHandle: TextView
    private lateinit var tvJoinDate: TextView
    
    // Recipe section views
    private lateinit var rvMyRecipes: RecyclerView
    private lateinit var tvMyRecipesEmpty: TextView
    
    // Favorites section views  
    private lateinit var rvFavorites: RecyclerView
    private lateinit var tvFavoritesEmpty: TextView
    
    // Settings section views
    private lateinit var cardEditProfile: MaterialCardView
    private lateinit var cardNotifications: MaterialCardView
    private lateinit var cardPrivacy: MaterialCardView
    private lateinit var cardHelpSupport: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupRecyclerViews()
        setupClickListeners()
        loadProfileData()
    }

    private fun initializeViews() {
        // Header views
        btnBack = findViewById(R.id.btn_back)
        btnSettings = findViewById(R.id.btn_settings)
        imgProfile = findViewById(R.id.img_profile)
        tvUsername = findViewById(R.id.tv_username)
        tvHandle = findViewById(R.id.tv_handle)
        tvJoinDate = findViewById(R.id.tv_join_date)
        
        // Recipe section views
        rvMyRecipes = findViewById(R.id.rv_my_recipes)
        tvMyRecipesEmpty = findViewById(R.id.tv_my_recipes_empty)
        
        // Favorites section views
        rvFavorites = findViewById(R.id.rv_favorites)
        tvFavoritesEmpty = findViewById(R.id.tv_favorites_empty)
        
        // Settings section views
        cardEditProfile = findViewById(R.id.card_edit_profile)
        cardNotifications = findViewById(R.id.card_notifications)
        cardPrivacy = findViewById(R.id.card_privacy)
        cardHelpSupport = findViewById(R.id.card_help_support)
    }

    private fun setupRecyclerViews() {
        // Setup My Recipes RecyclerView
        rvMyRecipes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // TODO: Set adapter when created
        
        // Setup Favorites RecyclerView
        rvFavorites.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // TODO: Set adapter when created
    }

    private fun setupClickListeners() {
        // Header navigation
        btnBack.setOnClickListener {
            onBackPressed()
        }
        
        btnSettings.setOnClickListener {
            // TODO: Navigate to settings when SettingsActivity is created
            // val intent = Intent(this, SettingsActivity::class.java)
            // startActivity(intent)
        }
        
        // Settings navigation
        cardEditProfile.setOnClickListener {
            // TODO: Navigate to edit profile when EditProfileActivity is created
            // val intent = Intent(this, EditProfileActivity::class.java)
            // startActivity(intent)
        }
        
        cardNotifications.setOnClickListener {
            // TODO: Navigate to notifications settings
            // val intent = Intent(this, NotificationSettingsActivity::class.java)
            // startActivity(intent)
        }
        
        cardPrivacy.setOnClickListener {
            // TODO: Navigate to privacy settings
            // val intent = Intent(this, PrivacySettingsActivity::class.java)
            // startActivity(intent)
        }
        
        cardHelpSupport.setOnClickListener {
            // TODO: Navigate to help & support
            // val intent = Intent(this, HelpSupportActivity::class.java)
            // startActivity(intent)
        }
    }

    private fun loadProfileData() {
        // TODO: Load profile data from repository
        // For now, set placeholder data
        tvUsername.text = "MixMate"
        tvHandle.text = "@mixmate"
        tvJoinDate.text = "Joined 2025"
        
        // TODO: Load profile picture
        // TODO: Load user's recipes
        // TODO: Load user's favorites
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}