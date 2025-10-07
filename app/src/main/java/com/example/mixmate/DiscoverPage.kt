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
        // Layout below the status bar
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_discover_page)

        // Match system nav bar to footer (light icons)
        val footerColor = ContextCompat.getColor(this, R.color.dark_brown_navbar)
        window.navigationBarColor = footerColor
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // ----- My Bar chips grid -----
        val spanCount = 2
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing)

        val rvBar: RecyclerView = findViewById(R.id.rv_bar_items)
        rvBar.layoutManager = GridLayoutManager(this, spanCount)
        rvBar.setHasFixedSize(true)
        rvBar.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val items = listOf(
            BarItem("Vodka", R.drawable.tequila),
            BarItem("Rum", R.drawable.tequila),
            BarItem("Tequila", R.drawable.tequila),
            BarItem("Whiskey", R.drawable.tequila),
            BarItem("Gin", R.drawable.tequila),
            BarItem("Juice", R.drawable.tequila)
        )
        rvBar.adapter = BarItemAdapter(items)

        // ----- Suggested cocktails grid -----
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
        // Adapter expects a MutableList in your project — convert once here
        rvSuggested.adapter = SuggestedCocktailAdapter(suggested.toMutableList())
    }
}
