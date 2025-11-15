package com.example.mixmate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixmate.CocktailApiRepository
import com.example.mixmate.GridSpacingItemDecoration
import com.example.mixmate.R
import com.example.mixmate.SuggestedCocktailAdapter
import com.example.mixmate.data.local.FavoriteEntity
import com.example.mixmate.ui.BaseActivity
import com.example.mixmate.ui.FooterTab
import com.example.mixmate.ui.favorites.SharedFavoritesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java

class HomePage : BaseActivity() {
    override fun activeTab() = FooterTab.HOME

    private lateinit var rvSuggested: RecyclerView
    private lateinit var loadingContainer: View
    private lateinit var emptyContainer: View
    private lateinit var suggestedAdapter: SuggestedCocktailAdapter

    private lateinit var favoritesViewModel: SharedFavoritesViewModel
    private val favoriteStates = mutableMapOf<String, Boolean>() // cache for quick lookups
    private lateinit var ivFeaturedDrink: ImageView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar


    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // RecyclerView setup
        rvSuggested = findViewById(R.id.recyclerTrending)
        loadingContainer = findViewById(R.id.loading_container_bar)
        emptyContainer = findViewById(R.id.empty_container_bar)
        ivFeaturedDrink = findViewById(R.id.mainTrendingDrink)
        toolbar = findViewById(R.id.toolbar)


        val spanCount = 2
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        rvSuggested.layoutManager = GridLayoutManager(this, spanCount)
        rvSuggested.setHasFixedSize(true)
        rvSuggested.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx))

        // Initialize favorites ViewModel
        val userId = UserManager.getCurrentUserUid() ?: UserManager.getUsername(this)
        favoritesViewModel = SharedFavoritesViewModel(userId)

        suggestedAdapter = SuggestedCocktailAdapter(
            items = mutableListOf(),
            onItemClick = null, // default click behavior
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

        // Load cocktails with images and favorite states
        lifecycleScope.launch {
            val apiItems = CocktailApiRepository.fetchCocktails(limit = 10)
            val data = if (apiItems.isNotEmpty()) CocktailImageProvider.enrichWithImages(apiItems) else emptyList()

            if (data.isNotEmpty()) {
                // First drink → featured
                val featured = data.first()
                featured.cocktailId?.let { id ->
                    val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
                    favoriteStates[id] = isFav
                    featured.isFavorite = isFav
                }

                // Load featured image
                Glide.with(this@HomePage)
                    .load(featured.imageUrl)
                    .into(ivFeaturedDrink)

                //RecyclerView
                val rest = data.drop(1)
                rest.forEach { cocktail ->
                    cocktail.cocktailId?.let { id ->
                        val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
                        favoriteStates[id] = isFav
                        cocktail.isFavorite = isFav
                    }
                }

                suggestedAdapter.replaceAll(rest)
                showContent()
            } else {
                showEmpty()
            }
        }

    }

    private suspend fun handleFavoriteToggle(cocktail: SuggestedCocktail, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            val userId = UserManager.getCurrentUserUid() ?: UserManager.getUsername(this@HomePage)
            val cocktailId = cocktail.cocktailId ?: cocktail.name.hashCode().toString()

            // Update cache immediately for smooth UI
            favoriteStates[cocktailId] = isFavorite

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

    override fun onResume() {
        super.onResume()
        // Refresh favorite states when returning to this page
        lifecycleScope.launch {
            suggestedAdapter.items.forEach { cocktail ->
                cocktail.cocktailId?.let { id ->
                    val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
                    favoriteStates[id] = isFav
                    cocktail.isFavorite = isFav
                }
            }
            // Notify adapter to update heart icons
            suggestedAdapter.notifyDataSetChanged()
        }
    }
}
