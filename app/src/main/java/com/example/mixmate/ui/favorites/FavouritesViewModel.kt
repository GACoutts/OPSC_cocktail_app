package com.example.mixmate.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mixmate.data.local.FavoriteEntity
import com.example.mixmate.data.repo.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavouritesViewModel(
    private val repo: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val query = MutableStateFlow("")

    // Emits all favourites, or filters by name when user types
    val items: StateFlow<List<FavoriteEntity>> =
        query
            .debounce(200)
            .flatMapLatest { q -> if (q.isBlank()) repo.getAll() else repo.searchByName(q) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { query.value = q }

    fun remove(id: String) {
        viewModelScope.launch { repo.deleteById(id) }
    }
}
