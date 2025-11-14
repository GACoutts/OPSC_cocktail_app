package com.example.mixmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.widget.ImageView
import android.widget.Toast
import android.content.Intent
import android.os.Build
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.view.View
import com.example.mixmate.data.local.FavoriteEntity
import com.example.mixmate.ui.favorites.SharedFavoritesViewModel
import kotlinx.coroutines.flow.firstOrNull

class MyBar : AppCompatActivity() {
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    private lateinit var favoritesViewModel: SharedFavoritesViewModel
    private val favoriteStates = mutableMapOf<String, Boolean>()
    private lateinit var suggestedAdapter: SuggestedCocktailAdapter
    private lateinit var rvSuggested: RecyclerView
    private lateinit var loadingContainer: View
    private lateinit var emptyContainer: View

    // Alcohol types and ingredients lists
    private lateinit var alcoholItems: List<BarItem>
    private lateinit var ingredientItems: List<BarItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure content is laid out below the system status bar
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_my_bar)

        // Initialize favorites ViewModel
        val userId = UserManager.getCurrentUserUid() ?: UserManager.getUsername(this)
        favoritesViewModel = SharedFavoritesViewModel(userId)

        // Make the system nav bar match the footer color and keep icons light
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Footer selection state
        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navDiscover = findViewById<ImageView>(R.id.nav_discover)
        val navList = findViewById<ImageView>(R.id.nav_list)
        val navFav = findViewById<ImageView>(R.id.nav_favourites)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)

        // Select the middle icon (list) for My Bar; unselect others
        navHome?.isSelected = false
        navDiscover?.isSelected = false
        navList?.isSelected = true
        navFav?.isSelected = false
        navProfile?.isSelected = false

        navHome?.setOnClickListener {
            startActivity(Intent(this, DiscoverPage::class.java))
        }
        navDiscover?.setOnClickListener {
            startActivity(Intent(this, DiscoverPage::class.java))
        }
        // Already on My Bar
        navList?.setOnClickListener { /* no-op */ }
        navFav?.setOnClickListener {
            startActivity(Intent(this, FavouritesActivity::class.java))
        }
        navProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val spanCount = 2
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing)

        // Setup Alcohol Types RecyclerView
        val rvAlcoholTypes: RecyclerView = findViewById(R.id.rv_alcohol_types)
        rvAlcoholTypes.layoutManager = GridLayoutManager(this, spanCount)
        rvAlcoholTypes.setHasFixedSize(true)
        rvAlcoholTypes.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val alcoholNames = resources.getStringArray(R.array.alcohol_type_options).toList().filter { it != "Reset" }
        alcoholItems = alcoholNames.map { alcohol ->
            BarItem(alcohol, R.drawable.ic_local_bar)
        }

        val alcoholAdapter = BarItemAdapter(alcoholItems) { _, _ ->
            lifecycleScope.launch {
                updateSuggestions()
            }
        }
        rvAlcoholTypes.adapter = alcoholAdapter

        // Setup Ingredients RecyclerView
        val rvIngredients: RecyclerView = findViewById(R.id.rv_bar_items)
        rvIngredients.layoutManager = GridLayoutManager(this, spanCount)
        rvIngredients.setHasFixedSize(true)
        rvIngredients.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val ingredientNames = resources.getStringArray(R.array.mybar_ingredients).toList()
        ingredientItems = ingredientNames.map { ingredient ->
            BarItem(ingredient, R.drawable.ic_local_bar)
        }

        val ingredientAdapter = BarItemAdapter(ingredientItems) { _, _ ->
            lifecycleScope.launch {
                updateSuggestions()
            }
        }
        rvIngredients.adapter = ingredientAdapter

        // Setup ingredient tab switching
        val ingredientTabs =
            findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.ingredient_tabs)
        val btnAlcohol = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_alcohol)
        val btnIngredients = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_ingredients)

        // Start with both collapsed
        btnIngredients.isChecked = false
        btnAlcohol.isChecked = false
        rvAlcoholTypes.visibility = View.GONE
        rvIngredients.visibility = View.GONE

        ingredientTabs.addOnButtonCheckedListener { group: com.google.android.material.button.MaterialButtonToggleGroup, checkedId: Int, isChecked: Boolean ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_alcohol -> {
                        rvAlcoholTypes.visibility = View.VISIBLE
                        rvIngredients.visibility = View.GONE
                        // Update suggested cocktails title constraint
                        updateSuggestedTitleConstraint(true)

                    }

                    R.id.btn_ingredients -> {
                        rvAlcoholTypes.visibility = View.GONE
                        rvIngredients.visibility = View.VISIBLE
                        // Update suggested cocktails title constraint
                        updateSuggestedTitleConstraint(false)
                    }
                }
            }
        }

        // Suggested Cocktails section with API-backed data and state handling
        rvSuggested = findViewById(R.id.rv_suggested)
        loadingContainer = findViewById(R.id.loading_container_bar)
        emptyContainer = findViewById(R.id.empty_container_bar)
        rvSuggested.layoutManager = GridLayoutManager(this, spanCount)
        rvSuggested.setHasFixedSize(true)
        rvSuggested.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        suggestedAdapter = SuggestedCocktailAdapter(
            items = mutableListOf(),
            onItemClick = null,
            onFavoriteClick = { cocktail, isFavorite ->
                lifecycleScope.launch {
                    handleFavoriteToggle(cocktail, isFavorite)
                }
            },
            getFavoriteState = { cocktailId ->
                favoriteStates[cocktailId] ?: false
            }
        )
        rvSuggested.adapter = suggestedAdapter

        showLoading()
        lifecycleScope.launch {
            loadDefaultSuggested()
        }
    }

    private fun showLoading() {
        loadingContainer.visibility = View.VISIBLE
        rvSuggested.visibility = View.GONE
        emptyContainer.visibility = View.GONE
    }

    private fun showContent() {
        loadingContainer.visibility = View.GONE
        rvSuggested.visibility = View.VISIBLE
        emptyContainer.visibility = View.GONE
    }

    private fun showEmpty() {
        loadingContainer.visibility = View.GONE
        rvSuggested.visibility = View.GONE
        emptyContainer.visibility = View.VISIBLE
    }

    private suspend fun updateSuggestions() {
        val selectedAlcohols = alcoholItems.filter { it.isSelected }.map { it.title }
        val selectedIngredients = ingredientItems.filter { it.isSelected }.map { it.title }

        val allSelected = selectedAlcohols + selectedIngredients

        if (allSelected.isNotEmpty()) {
            loadSuggestedByMultipleIngredients(allSelected)
        } else {
            loadDefaultSuggested()
        }
    }

    private suspend fun loadDefaultSuggested() {
        showLoading()
        try {
            // Get rotating alcohol type
            val alcoholType = MyBarRotatingHelper.getRotatingAlcoholType(this)
            android.util.Log.d("MyBar", "Loading default cocktails for: $alcoholType")

            // Load cocktails filtered by the rotating alcohol type
            loadSuggestedByIngredient(alcoholType)
        } catch (e: Exception) {
            android.util.Log.e("MyBar", "Error loading rotating suggestions, fallback to generic", e)
            // Fallback to generic cocktails
            val apiItems = CocktailApiRepository.fetchCocktails(limit = 50)
            val data = if (apiItems.isNotEmpty()) CocktailImageProvider.enrichWithImages(apiItems) else emptyList()

            // Load favorite states
            data.forEach { cocktail ->
                cocktail.cocktailId?.let { id ->
                    val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
                    favoriteStates[id] = isFav
                    cocktail.isFavorite = isFav
                }
            }

            updateSuggestedList(data)
        }
    }

    private suspend fun loadSuggestedByIngredient(ingredient: String) {
        showLoading()
        val apiResponse = try {
            val api = com.example.mixmate.data.remote.CocktailApi.create()
            withContext(Dispatchers.IO) {
                api.filterByIngredient(ingredient)
            }
        } catch (e: Exception) {
            android.util.Log.e("MyBar", "Error filtering by ingredient: $ingredient", e)
            null
        }

        val cocktails = apiResponse?.drinks?.mapIndexed { index, drink ->
            val rating = 5.0 - (index * 0.1).coerceAtMost(2.0) // Generate ratings based on position
            SuggestedCocktail(
                name = drink.strDrink ?: "Unknown",
                rating = rating,
                category = ingredient,
                imageUrl = drink.strDrinkThumb,
                cocktailId = drink.idDrink,
                isFavorite = false
            )
        } ?: emptyList()

        android.util.Log.d("MyBar", "Loaded ${cocktails.size} cocktails for $ingredient")

        // Load favorite states
        cocktails.forEach { cocktail ->
            cocktail.cocktailId?.let { id ->
                val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
                favoriteStates[id] = isFav
                cocktail.isFavorite = isFav
            }
        }

        updateSuggestedList(cocktails)
    }

    private suspend fun loadSuggestedByMultipleIngredients(ingredients: List<String>) {
        showLoading()
        try {
            val api = com.example.mixmate.data.remote.CocktailApi.create()
                        val allCocktails = mutableSetOf<SuggestedCocktail>()

            android.util.Log.d("MyBar", "Loading cocktails for ${ingredients.size} ingredients: $ingredients")

            // Fetch cocktails for each selected ingredient
            for (ingredient in ingredients) {
                try {
                    val apiResponse = withContext(Dispatchers.IO) {
                        api.filterByIngredient(ingredient)
                                                }
                    apiResponse.drinks?.forEachIndexed { index, drink ->
                        if (drink.idDrink != null && drink.strDrink != null) {
                            // Calculate rating based on position (popularity)
                            val rating = 5.0 - (index * 0.1).coerceAtMost(2.0)

                            allCocktails.add(
                                SuggestedCocktail(
                                    name = drink.strDrink,
                                    rating = rating,
                                    category = ingredient,
                                    imageUrl = drink.strDrinkThumb,
                                    cocktailId = drink.idDrink,
                                    isFavorite = false
                                )
                            )
                        }
                    }
                                android.util.Log.d("MyBar", "Added cocktails for $ingredient, total now: ${allCocktails.size}")
                } catch (e: Exception) {
                    android.util.Log.e("MyBar", "Error loading ingredient: $ingredient", e)
                    // Continue with next ingredient if one fails
                    continue
                }
            }

            android.util.Log.d("MyBar", "Total unique cocktails loaded: ${allCocktails.size}")

            // Load favorite states
            allCocktails.forEach { cocktail ->
                cocktail.cocktailId?.let { id ->
                    val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
                    favoriteStates[id] = isFav
                    cocktail.isFavorite = isFav
                }
            }

            updateSuggestedList(allCocktails.toList())
        } catch (e: Exception) {
            android.util.Log.e("MyBar", "Error in loadSuggestedByMultipleIngredients", e)
            updateSuggestedList(emptyList())
        }
    }

    private suspend fun updateSuggestedList(data: List<SuggestedCocktail>) {
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

    private suspend fun handleFavoriteToggle(cocktail: SuggestedCocktail, isFavorite: Boolean) {
        val userId = UserManager.getCurrentUserUid() ?: UserManager.getUsername(this@MyBar)
        val cocktailId = cocktail.cocktailId ?: cocktail.name.hashCode().toString()

        // Update cache immediately
        favoriteStates[cocktailId] = isFavorite

        // Use shared ViewModel for consistency
        val favoriteEntity = FavoriteEntity(
            cocktailId = cocktailId,
            name = cocktail.name,
            imageUrl = cocktail.imageUrl ?: "",
            ingredients = "",
            instructions = "",
            userId = userId
        )

        favoritesViewModel.toggleFavorite(favoriteEntity, !isFavorite)
    }

    private fun updateSuggestedTitleConstraint(isAlcoholTab: Boolean) {
        // This method can be used to dynamically update constraints if needed
        // For now, the layout handles both cases with the fixed constraint to rv_alcohol_types
        // which works because rv_bar_items has the same top constraint
    }
}
