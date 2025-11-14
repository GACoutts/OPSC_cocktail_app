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
import com.example.mixmate.ui.discover.FilterViewModel
import kotlinx.coroutines.flow.firstOrNull
import androidx.lifecycle.ViewModelProvider

class DiscoverPage : AppCompatActivity() {
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }


    private lateinit var favoritesViewModel: SharedFavoritesViewModel
    private lateinit var filterViewModel: FilterViewModel
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

        // Initialize filter ViewModel
        filterViewModel = ViewModelProvider(this).get(FilterViewModel::class.java)

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

        // Setup dropdown adapters and listeners
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

        // Setup filter listeners
        ingredientView?.setOnItemClickListener { _, _, position, _ ->
            val selected = resources.getStringArray(R.array.ingredient_options)[position]
            filterViewModel.filterByIngredient(selected)
        }

        alcoholView?.setOnItemClickListener { _, _, position, _ ->
            val selected = resources.getStringArray(R.array.alcohol_type_options)[position]
            filterViewModel.filterByCategory(selected)
        }

        ratingView?.setOnItemClickListener { _, _, position, _ ->
            val ratings = resources.getStringArray(R.array.rating_options)
            val selected = ratings[position]
            
            val minRating = when (selected) {
                "4.5+" -> 4.5
                "4.0+" -> 4.0
                "3.5+" -> 3.5
                "3.0+" -> 3.0
                else -> 0.0
            }
            
            filterViewModel.filterByRating(minRating)
        }

        // Do not set default text; keep empty. Ensure tapping opens the menu.
        ingredientView?.setOnClickListener { ingredientView.showDropDown() }
        alcoholView?.setOnClickListener { alcoholView.showDropDown() }
        ratingView?.setOnClickListener { ratingView.showDropDown() }

        // Setup sort button listeners
        val btnSortPopular = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_sort_popular)
        val btnSortNewest = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_sort_newest)
        val btnSortTopRated = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_sort_top_rated)

        btnSortPopular?.setOnClickListener {
            filterViewModel.setSortOrder(FilterViewModel.SortOrder.POPULAR)
        }
        btnSortNewest?.setOnClickListener {
            filterViewModel.setSortOrder(FilterViewModel.SortOrder.NEWEST)
        }
        btnSortTopRated?.setOnClickListener {
            filterViewModel.setSortOrder(FilterViewModel.SortOrder.TOP_RATED)
        }

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
            
            // Initialize filter ViewModel with default cocktails
            filterViewModel.setInitialCocktails(data)
            
            if (data.isNotEmpty()) {
                suggestedAdapter.replaceAll(data)
                showContent()
            } else {
                showEmpty()
            }

            // Observe loading state
            lifecycleScope.launch {
                filterViewModel.isLoading.collect { isLoading ->
                    if (isLoading) {
                        showLoading()
                    }
                }
            }

            // Observe filtered cocktails
            lifecycleScope.launch {
                filterViewModel.filteredCocktails.collect { filtered ->
                    suggestedAdapter.replaceAll(filtered)
                    if (filtered.isEmpty() && data.isNotEmpty()) {
                        showEmpty()
                    } else if (filtered.isNotEmpty()) {
                        showContent()
                    }
                }
            }

            // Observe errors
            lifecycleScope.launch {
                filterViewModel.error.collect { error ->
                    if (!error.isNullOrBlank()) {
                        Toast.makeText(this@DiscoverPage, error, Toast.LENGTH_SHORT).show()
                    }
                }
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
