package com.example.mixmate.ui.details

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.mixmate.DiscoverPage
import com.example.mixmate.FavouritesActivity
import com.example.mixmate.HomePage
import com.example.mixmate.MyBar
import com.example.mixmate.ProfileActivity
import com.example.mixmate.R
import com.example.mixmate.UserManager
import com.example.mixmate.ui.favorites.SharedFavoritesViewModel
import com.example.mixmate.data.local.FavoriteEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class RecipeDetailsActivity : ComponentActivity() {

    private lateinit var vm: RecipeDetailsViewModel
    private lateinit var sharedFavoritesViewModel: SharedFavoritesViewModel

    // keep your locale hook
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(com.example.mixmate.LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        // Setup back button
        val backButton = findViewById<ImageView>(R.id.header_back)
        backButton?.visibility = android.view.View.VISIBLE
        backButton?.setOnClickListener {
            finish()
        }

        // Setup footer navigation
        setupFooterNavigation()

        // views present in activity_recipe_details.xml
        val ivPhoto = findViewById<ImageView>(R.id.ivPhoto)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvIngredients = findViewById<TextView>(R.id.tvIngredients)
        val tvInstructions = findViewById<TextView>(R.id.tvInstructions)
        val btnFav = findViewById<ImageButton>(R.id.btnFav)

        // VM
        val userId = UserManager.getCurrentUserUid() ?: "default_user"
        vm = ViewModelProvider(
            this,
            RecipeDetailsViewModelFactory(userId)
        )[RecipeDetailsViewModel::class.java]

        // Initialize SharedFavoritesViewModel for synced favorites
        sharedFavoritesViewModel = SharedFavoritesViewModel(userId)

        // always set initial (from adapter extras)
        val cocktailName = intent.getStringExtra("cocktail_name").orEmpty()
        val cocktailImage = intent.getStringExtra("cocktail_image").orEmpty()
        vm.setInitial(cocktailName, cocktailImage)

        // then load full details if we have an id, or search by name as fallback
        val cocktailId = intent.getStringExtra("cocktail_id")
        if (!cocktailId.isNullOrBlank()) {
            vm.load(cocktailId)
        } else if (cocktailName.isNotBlank()) {
            // Fallback: Try to find the cocktail by name
            vm.findByNameThenLoad(cocktailName)
        }

        // observe UI state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.ui.collectLatest { s ->
                    tvName.text = s.name
                    tvIngredients.text = s.ingredients
                    tvInstructions.text = s.instructions

                    if (s.imageUrl.isNotBlank()) {
                        Glide.with(this@RecipeDetailsActivity)
                            .load(s.imageUrl)
                            .placeholder(R.drawable.ic_default_cocktail)
                            .error(R.drawable.ic_default_cocktail)
                            .centerCrop()
                            .into(ivPhoto)
                    } else {
                        ivPhoto.setImageResource(R.drawable.ic_default_cocktail)
                    }

                    btnFav.setImageResource(
                        if (s.isFavorited) R.drawable.ic_heart_filled
                        else R.drawable.ic_heart_outline
                    )

                    s.error?.let {
                        if (it.isNotBlank()) {
                            Toast.makeText(this@RecipeDetailsActivity, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        btnFav.setOnClickListener {
            lifecycleScope.launch {
                val currentState = vm.ui.value
                val cocktailId = currentState.id

                if (cocktailId.isBlank()) {
                    Toast.makeText(this@RecipeDetailsActivity, "Unable to favorite this cocktail", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Check if currently favorited
                val isFavorited = sharedFavoritesViewModel.isFavorite(cocktailId).firstOrNull() ?: false

                if (isFavorited) {
                    // Remove from favorites
                    val favoriteEntity = FavoriteEntity(
                        cocktailId = cocktailId,
                        name = currentState.name,
                        imageUrl = currentState.imageUrl,
                        ingredients = currentState.ingredients,
                        instructions = currentState.instructions,
                        userId = userId
                    )
                    sharedFavoritesViewModel.toggleFavorite(favoriteEntity, true)
                    Toast.makeText(this@RecipeDetailsActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                } else {
                    // Add to favorites
                    val favoriteEntity = FavoriteEntity(
                        cocktailId = cocktailId,
                        name = currentState.name,
                        imageUrl = currentState.imageUrl,
                        ingredients = currentState.ingredients,
                        instructions = currentState.instructions,
                        userId = userId
                    )
                    sharedFavoritesViewModel.toggleFavorite(favoriteEntity, false)
                    Toast.makeText(this@RecipeDetailsActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                }

                // Update local VM state
                vm.toggleFavorite()
            }
        }

        // Observe favorite state changes from SharedFavoritesViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.ui.collectLatest { state ->
                        if (state.id.isNotBlank()) {
                            sharedFavoritesViewModel.isFavorite(state.id).collectLatest { isFav ->
                                btnFav.setImageResource(
                                    if (isFav) R.drawable.ic_heart_filled
                                    else R.drawable.ic_heart_outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupFooterNavigation() {
        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navDiscover = findViewById<ImageView>(R.id.nav_discover)
        val navList = findViewById<ImageView>(R.id.nav_list)
        val navFav = findViewById<ImageView>(R.id.nav_favourites)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)

        navHome?.setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
            finish()
        }
        navDiscover?.setOnClickListener {
            startActivity(Intent(this, DiscoverPage::class.java))
            finish()
        }
        navList?.setOnClickListener {
            startActivity(Intent(this, MyBar::class.java))
            finish()
        }
        navFav?.setOnClickListener {
            startActivity(Intent(this, FavouritesActivity::class.java))
            finish()
        }
        navProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}

class RecipeDetailsViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailsViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
