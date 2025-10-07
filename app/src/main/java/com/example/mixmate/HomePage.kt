package com.example.mixmate

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmate.CocktailApiRepository
import com.example.mixmate.GridSpacingItemDecoration
import com.example.mixmate.R
import com.example.mixmate.SuggestedCocktailAdapter
import com.example.mixmate.ui.BaseActivity
import com.example.mixmate.ui.FooterTab
import kotlinx.coroutines.launch

abstract class HomePage : BaseActivity() {
    override fun activeTab() = FooterTab.HOME

    private lateinit var rvSuggested: RecyclerView
    private lateinit var loadingContainer: View
    private lateinit var emptyContainer: View
    private lateinit var suggestedAdapter: SuggestedCocktailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        //Header code goes here

        // RecyclerView setup
        rvSuggested = findViewById(R.id.recyclerTrending)
        loadingContainer = findViewById(R.id.loading_container_bar)
        emptyContainer = findViewById(R.id.empty_container_bar)

        val spanCount = 2
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        rvSuggested.layoutManager = GridLayoutManager(this, spanCount)
        rvSuggested.setHasFixedSize(true)
        rvSuggested.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx))

        suggestedAdapter = SuggestedCocktailAdapter(mutableListOf())
        rvSuggested.adapter = suggestedAdapter

        showLoading()

        lifecycleScope.launch {
            val apiItems = CocktailApiRepository.fetchCocktails(limit = 10)
            if (apiItems.isNotEmpty()) {
                suggestedAdapter.replaceAll(apiItems)
                showContent()
            } else {
                showEmpty()
            }
        }
    }

    //footer code goes here


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
}
