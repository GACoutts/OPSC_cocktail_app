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
        navFav?.setOnClickListener { Toast.makeText(this, getString(R.string.favourites_coming_soon), Toast.LENGTH_SHORT).show() }
        navProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val recycler: RecyclerView = findViewById(R.id.rv_bar_items)
        val spanCount = 2
        recycler.layoutManager = GridLayoutManager(this, spanCount)
        recycler.setHasFixedSize(true)

        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recycler.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val items = listOf(
            BarItem("Vodka", R.drawable.vodka),
            BarItem("Rum", R.drawable.rum),
            BarItem("Tequila", R.drawable.tequila),
            BarItem("Whiskey", R.drawable.whiskey),
            BarItem("Gin", R.drawable.gin),
            BarItem("Juice", R.drawable.juice),
        )
        recycler.adapter = BarItemAdapter(items)

        // Suggested Cocktails section with API-backed data and state handling
        val rvSuggested: RecyclerView = findViewById(R.id.rv_suggested)
        val loadingContainer: View = findViewById(R.id.loading_container_bar)
        val emptyContainer: View = findViewById(R.id.empty_container_bar)
        rvSuggested.layoutManager = GridLayoutManager(this, spanCount)
        rvSuggested.setHasFixedSize(true)
        rvSuggested.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val suggestedAdapter = SuggestedCocktailAdapter(
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
}