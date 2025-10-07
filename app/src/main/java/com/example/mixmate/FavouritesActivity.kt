package com.example.mixmate

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import com.example.mixmate.ui.BaseActivity
import com.example.mixmate.ui.FooterTab
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmate.ui.favorites.FavouritesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener
import android.text.Editable


class FavouritesActivity : BaseActivity() {
    override fun activeTab() = com.example.mixmate.ui.FooterTab.FAVOURITES

    private lateinit var vm: FavouritesViewModel
    private lateinit var adapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        val userId = UserManager.getCurrentUserUid() ?: UserManager.getUsername(this)
        vm = ViewModelProvider(this, FavouritesViewModelFactory(userId))[FavouritesViewModel::class.java]

        val search = findViewById<EditText>(R.id.etSearch)
        val list = findViewById<RecyclerView>(R.id.rvFavs)

        adapter = FavoritesAdapter(
            onClick = { fav ->
                // Open Recipe Details using the saved ID
                startActivity(
                    Intent(this, com.example.mixmate.ui.details.RecipeDetailsActivity::class.java)
                        .putExtra("cocktail_id", fav.cocktailId)
                )
            },
            onDelete = { fav -> vm.remove(fav.cocktailId) }
        )

        list.layoutManager = GridLayoutManager(this, 2)
        list.adapter = adapter

        search.addTextChangedListener { text ->
            vm.setQuery(text?.toString().orEmpty())
        }

        lifecycleScope.launch {
            vm.items.collectLatest { items ->
                adapter.submitList(items)
            }
        }
    }
}

class FavouritesViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavouritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavouritesViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
