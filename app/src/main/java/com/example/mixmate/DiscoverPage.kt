package com.example.mixmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.os.Build
import android.widget.ArrayAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import android.content.Intent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import android.view.View
import kotlinx.coroutines.launch
import com.example.mixmate.data.local.FavoriteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.mixmate.ui.favorites.SharedFavoritesViewModel
import kotlinx.coroutines.flow.firstOrNull

class DiscoverPage : AppCompatActivity() {
    
    private lateinit var favoritesViewModel: SharedFavoritesViewModel
    private val favoriteStates = mutableMapOf<String, Boolean>()  // Cache for quick lookups
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_discover_page)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Initialize favorites ViewModel
        val userId = UserManager.getCurrentUserUid() ?: UserManager.getUsername(this)
        favoritesViewModel = SharedFavoritesViewModel(userId)

        // Footer navigation wiring
        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navDiscover = findViewById<ImageView>(R.id.nav_discover)
        val navList = findViewById<ImageView>(R.id.nav_list)
        val navFav = findViewById<ImageView>(R.id.nav_favourites)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)

        navDiscover?.isSelected = true

        navHome?.setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
            finish()
        }
        navDiscover?.setOnClickListener { /* already here */ }
        navList?.setOnClickListener {
            startActivity(Intent(this, MyBar::class.java))
        }
        navFav?.setOnClickListener {
            startActivity(Intent(this, FavouritesActivity::class.java))
        }
        navProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Setup dropdown adapters (textless pills)
        val ingredientView = findViewById<MaterialAutoCompleteTextView>(R.id.ac_filter_ingredient)
        val alcoholView = findViewById<MaterialAutoCompleteTextView>(R.id.ac_filter_alcohol)
        val ratingView = findViewById<MaterialAutoCompleteTextView>(R.id.ac_filter_rating)

        fun adapterFromArray(arrayId: Int) = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(arrayId)
        )

        ingredientView?.setAdapter(adapterFromArray(R.array.ingredient_options))
        alcoholView?.setAdapter(adapterFromArray(R.array.alcohol_type_options))
        ratingView?.setAdapter(adapterFromArray(R.array.rating_options))

        // Do not set default text; keep empty. Ensure tapping opens the menu.
        ingredientView?.setOnClickListener { ingredientView.showDropDown() }
        alcoholView?.setOnClickListener { alcoholView.showDropDown() }
        ratingView?.setOnClickListener { ratingView.showDropDown() }

        // Suggested Cocktails grid (API backed with loading / empty states)
        val rvSuggested: RecyclerView = findViewById(R.id.rv_suggested)
        val loadingContainer: View = findViewById(R.id.loading_container_discover)
        val emptyContainer: View = findViewById(R.id.empty_container_discover)
        val spanCount = 2
        rvSuggested.layoutManager = GridLayoutManager(this, spanCount)
        rvSuggested.setHasFixedSize(true)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        rvSuggested.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val suggestedAdapter = SuggestedCocktailAdapter(
            items = mutableListOf(),
            onItemClick = null, // Use default behavior
            onFavoriteClick = { cocktail, isFavorite ->
                // Handle favorite toggle using shared ViewModel
                lifecycleScope.launch {
                    handleFavoriteToggle(cocktail, isFavorite)
                }
            },
            getFavoriteState = { cocktailId ->
                // Return cached favorite state for immediate UI update
                favoriteStates[cocktailId] ?: false
            }
        )
        rvSuggested.adapter = suggestedAdapter

        fun showLoading() {
            loadingContainer.visibility = View.VISIBLE
            rvSuggested.visibility = View.GONE
            emptyContainer.visibility = View.GONE
        }
        fun showContent() {
            loadingContainer.visibility = View.GONE
            rvSuggested.visibility = View.VISIBLE
            emptyContainer.visibility = View.GONE
        }
        fun showEmpty() {
            loadingContainer.visibility = View.GONE
            rvSuggested.visibility = View.GONE
            emptyContainer.visibility = View.VISIBLE
        }

        showLoading()
        lifecycleScope.launch {
            val apiItems = CocktailApiRepository.fetchCocktails(limit = 10)
            val data = if (apiItems.isNotEmpty()) CocktailImageProvider.enrichWithImages(apiItems) else emptyList()
            
            // Load favorite states for all cocktails
            data.forEach { cocktail ->
                cocktail.cocktailId?.let { id ->
                    val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
                    favoriteStates[id] = isFav
                    cocktail.isFavorite = isFav
                }
            }
            
            if (data.isNotEmpty()) {
                suggestedAdapter.replaceAll(data)
                showContent()
            } else {
                showEmpty()
            }
        }
    }

    private suspend fun handleFavoriteToggle(cocktail: SuggestedCocktail, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            val userId = UserManager.getCurrentUserUid() ?: UserManager.getUsername(this@DiscoverPage)
            val cocktailId = cocktail.cocktailId ?: cocktail.name.hashCode().toString()
            
            // Update cache immediately
            favoriteStates[cocktailId] = isFavorite
            
            // Use shared ViewModel for consistency
            val favoriteEntity = FavoriteEntity(
                cocktailId = cocktailId,
                name = cocktail.name,
                imageUrl = cocktail.imageUrl ?: "",
                ingredients = "", // Will be populated when recipe details are loaded
                instructions = "", // Will be populated when recipe details are loaded
                userId = userId
            )
            
            favoritesViewModel.toggleFavorite(favoriteEntity, !isFavorite)
        }
    }
}
