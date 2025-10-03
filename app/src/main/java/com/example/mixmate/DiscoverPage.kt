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
import kotlinx.coroutines.launch

class DiscoverPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_discover_page)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

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
        navFav?.setOnClickListener { Toast.makeText(this, "Favourites coming soon", Toast.LENGTH_SHORT).show() }
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

        // Suggested Cocktails grid (API backed with fallback)
        val rvSuggested: RecyclerView = findViewById(R.id.rv_discover_suggested)
        val spanCount = 2
        rvSuggested.layoutManager = GridLayoutManager(this, spanCount)
        rvSuggested.setHasFixedSize(true)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        rvSuggested.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val fallbackSuggested = listOf(
            SuggestedCocktail("Cosmopolitan", 4.5, "Vodka", R.drawable.cosmopolitan),
            SuggestedCocktail("Mojito", 4.2, "Rum", R.drawable.cosmopolitan),
            SuggestedCocktail("Margarita", 4.7, "Tequila", R.drawable.cosmopolitan),
            SuggestedCocktail("Old Fashioned", 4.6, "Whiskey", R.drawable.cosmopolitan),
            SuggestedCocktail("Martini", 3.5, "Tequila", R.drawable.cosmopolitan),
            SuggestedCocktail("Daiquiri", 4.6, "Whiskey", R.drawable.cosmopolitan)
        )
        val suggestedAdapter = SuggestedCocktailAdapter(fallbackSuggested.toMutableList())
        rvSuggested.adapter = suggestedAdapter

        lifecycleScope.launch {
            val apiItems = CocktailApiRepository.fetchCocktails(limit = 10)
            if (apiItems.isNotEmpty()) {
                suggestedAdapter.replaceAll(apiItems)
            } else {
                Toast.makeText(this@DiscoverPage, "Using offline cocktail list", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
