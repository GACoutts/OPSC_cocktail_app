package com.example.mixmate.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mixmate.data.local.FavoriteEntity
import com.example.mixmate.data.repo.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for favorites functionality across all screens.
 * This ensures a single source of truth for favorite state.
 */
class SharedFavoritesViewModel(
    private val userId: String,
    private val repo: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    /**
     * Check if a cocktail is favorited (reactive Flow).
     * Subscribe to this in UI to get real-time updates.
     */
    fun isFavorite(cocktailId: String): Flow<Boolean> {
        return repo.isFavoriteFlow(cocktailId, userId)
    }

    /**
     * Toggle favorite status for a cocktail.
     * @param favorite The FavoriteEntity to add (if favoriting) or remove (if unfavoriting)
     * @param currentlyFavorited Whether the item is currently favorited
     */
    fun toggleFavorite(favorite: FavoriteEntity, currentlyFavorited: Boolean) {
        viewModelScope.launch {
            if (currentlyFavorited) {
                repo.deleteById(favorite.cocktailId, userId)
            } else {
                repo.upsert(favorite)
            }
        }
    }

    /**
     * Add to favorites.
     */
    fun addFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repo.upsert(favorite)
        }
    }

    /**
     * Remove from favorites by ID.
     */
    fun removeFavorite(cocktailId: String) {
        viewModelScope.launch {
            repo.deleteById(cocktailId, userId)
        }
    }
}
