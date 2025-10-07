package com.example.mixmate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.example.mixmate.data.remote.FirebaseRecipeRepository
import com.example.mixmate.data.repository.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileActivity : AppCompatActivity() {

    // Repository and adapter
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var myRecipesAdapter: MyRecipesAdapter
    private val activityScope = CoroutineScope(Dispatchers.Main)

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
    private lateinit var cardLogout: MaterialCardView
    
    // FAB for creating recipes
    private lateinit var fabCreateRecipe: FloatingActionButton
    
    // Footer navigation views
    private lateinit var navHome: ImageView
    private lateinit var navDiscover: ImageView
    private lateinit var navList: ImageView
    private lateinit var navFavourites: ImageView
    private lateinit var navProfile: ImageView

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
        updateNavigationState()
        initializeRepository()
        loadProfileData()
        loadMyRecipes()
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
        cardLogout = findViewById(R.id.card_logout)
        
        // FAB
        fabCreateRecipe = findViewById(R.id.fab_create_recipe)
        
        // Footer navigation views
        navHome = findViewById(R.id.nav_home)
        navDiscover = findViewById(R.id.nav_discover)
        navList = findViewById(R.id.nav_list)
        navFavourites = findViewById(R.id.nav_favourites)
        navProfile = findViewById(R.id.nav_profile)
    }

    private fun setupRecyclerViews() {
        // Setup My Recipes RecyclerView
        myRecipesAdapter = MyRecipesAdapter { recipe ->
            // Navigate to RecipeDetailActivity
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id)
            startActivity(intent)
        }
        rvMyRecipes.adapter = myRecipesAdapter
        rvMyRecipes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
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
            Log.d("ProfileActivity", "Settings button clicked")
            Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()
            try {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error starting SettingsActivity: ${e.message}")
                Toast.makeText(this, "Error opening Settings", Toast.LENGTH_SHORT).show()
            }
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
        
        cardLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        
        // FAB click listener
        fabCreateRecipe.setOnClickListener {
            Log.d("ProfileActivity", "FAB clicked - navigating to SubmitRecipeActivity")
            Toast.makeText(this, "Opening Submit Recipe...", Toast.LENGTH_SHORT).show()
            try {
                val intent = Intent(this, SubmitRecipeActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error starting SubmitRecipeActivity: ${e.message}")
                Toast.makeText(this, "Error opening Submit Recipe", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Footer navigation listeners
        navHome.setOnClickListener {
            val intent = Intent(this, DiscoverPage::class.java)
            startActivity(intent)
        }
        
        navDiscover.setOnClickListener {
            val intent = Intent(this, DiscoverPage::class.java)
            startActivity(intent)
        }
        
        navList.setOnClickListener {
            val intent = Intent(this, MyBar::class.java)
            startActivity(intent)
        }
        
        navFavourites.setOnClickListener {
            // TODO: Navigate to favourites page when created
            // val intent = Intent(this, FavouritesActivity::class.java)
            // startActivity(intent)
        }
        
        navProfile.setOnClickListener {
            // Already on profile page - do nothing or scroll to top
        }
    }

    private fun updateNavigationState() {
        // Set profile as selected (current page)
        navProfile.isSelected = true
        
        // Ensure other navigation items are not selected
        navHome.isSelected = false
        navDiscover.isSelected = false
        navList.isSelected = false
        navFavourites.isSelected = false
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        // Sign out user from Firebase and clear local data
        UserManager.signOut(this)
        
        // Navigate back to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun initializeRepository() {
        val customRecipeDao = MixMateApp.db.customRecipeDao()
        val firebaseRepository = FirebaseRecipeRepository()
        recipeRepository = RecipeRepository(customRecipeDao, firebaseRepository, activityScope)
    }
    
    private fun loadMyRecipes() {
        val userId = UserManager.getCurrentUserUid() ?: UserManager.getUsername(this)
        
        activityScope.launch {
            recipeRepository.getAllRecipes(userId).collect { recipes ->
                myRecipesAdapter.updateRecipes(recipes)
                
                // Show/hide empty state
                if (recipes.isEmpty()) {
                    tvMyRecipesEmpty.visibility = View.VISIBLE
                    rvMyRecipes.visibility = View.GONE
                } else {
                    tvMyRecipesEmpty.visibility = View.GONE
                    rvMyRecipes.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadProfileData() {
        // Load user data from UserManager
        tvUsername.text = UserManager.getDisplayName(this)
        tvHandle.text = UserManager.getUsername(this)
        tvJoinDate.text = UserManager.getJoinDate(this)
        
        // TODO: Load profile picture from UserManager.getProfilePictureUri()
        // TODO: Load user's favorites from Room database
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}