package com.example.mixmate

import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmate.ui.BaseActivity
import com.example.mixmate.ui.FooterTab

class DiscoverPage : BaseActivity() {
    override fun activeTab() = FooterTab.DISCOVER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure content is laid out below the system status bar
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_discover_page)

        // Make the system nav bar match the footer color and keep icons light
        val footerColor = ContextCompat.getColor(this, R.color.dark_brown_navbar)
        window.navigationBarColor = footerColor
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // --- My Bar grid ---
        val recycler: RecyclerView = findViewById(R.id.rv_bar_items)
        val spanCount = 2
        recycler.layoutManager = GridLayoutManager(this, spanCount)
        recycler.setHasFixedSize(true)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recycler.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val items = listOf(
            BarItem("Vodka", R.drawable.tequila),
            BarItem("Rum", R.drawable.tequila),
            BarItem("Tequila", R.drawable.tequila),
            BarItem("Whiskey", R.drawable.tequila),
            BarItem("Gin", R.drawable.tequila),
            BarItem("Juice", R.drawable.tequila)
        )
        recycler.adapter = BarItemAdapter(items)

        // --- Suggested Cocktails grid ---
        val rvSuggested: RecyclerView = findViewById(R.id.rv_suggested)
        rvSuggested.layoutManager = GridLayoutManager(this, spanCount)
        rvSuggested.setHasFixedSize(true)
        rvSuggested.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val suggested = listOf(
            SuggestedCocktail("Cosmopolitan", 4.5, "Vodka", R.drawable.cosmopolitan),
            SuggestedCocktail("Mojito", 4.2, "Rum", R.drawable.cosmopolitan),
            SuggestedCocktail("Margarita", 4.7, "Tequila", R.drawable.cosmopolitan),
            SuggestedCocktail("Old Fashioned", 4.6, "Whiskey", R.drawable.cosmopolitan)
        )
        rvSuggested.adapter = SuggestedCocktailAdapter(suggested)
    }
}
