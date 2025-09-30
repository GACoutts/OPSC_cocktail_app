package com.example.mixmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.WindowCompat

class DiscoverPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure content is laid out below the system status bar
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_discover_page)

        val recycler: RecyclerView = findViewById(R.id.rv_bar_items)
        val spanCount = 2
        recycler.layoutManager = GridLayoutManager(this, spanCount)
        recycler.setHasFixedSize(true)

        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recycler.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, includeEdge = false))

        val items = listOf(
            BarItem("Vodka", R.drawable.ic_heart),
            BarItem("Rum", R.drawable.ic_heart),
            BarItem("Tequila", R.drawable.ic_heart),
            BarItem("Whiskey", R.drawable.ic_heart),
            BarItem("Gin", R.drawable.ic_heart),
            BarItem("Juice", R.drawable.ic_heart)
        )
        recycler.adapter = BarItemAdapter(items)

        // Suggested Cocktails section
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