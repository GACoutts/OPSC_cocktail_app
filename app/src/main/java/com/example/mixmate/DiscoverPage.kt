package com.example.mixmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DiscoverPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            BarItem("Orange Juice", R.drawable.ic_heart)
        )
        recycler.adapter = BarItemAdapter(items)
    }
}