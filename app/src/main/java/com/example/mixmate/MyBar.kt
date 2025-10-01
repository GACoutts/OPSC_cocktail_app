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

class MyBar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure content is laid out below the system status bar
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_my_bar)

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

        navHome?.setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
            finish()
        }
        navDiscover?.setOnClickListener {
            startActivity(Intent(this, DiscoverPage::class.java))
            finish()
        }
        navList?.setOnClickListener { Toast.makeText(this, "List coming soon", Toast.LENGTH_SHORT).show() }
        navFav?.setOnClickListener { Toast.makeText(this, "Favourites coming soon", Toast.LENGTH_SHORT).show() }
        navProfile?.setOnClickListener { Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show() }

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