package com.example.mixmate.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mixmate.SuggestedCocktail
import com.example.mixmate.data.remote.CocktailApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing filters on the Discover page
 * Handles ingredient, rating, and sort filtering
 */
class FilterViewModel : ViewModel() {

    private val api = CocktailApi.create()

    // State: Filter selections
    private val _selectedIngredient = MutableStateFlow<String?>(null)
    val selectedIngredient: StateFlow<String?> = _selectedIngredient

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _selectedRating = MutableStateFlow<Double?>(null)
    val selectedRating: StateFlow<Double?> = _selectedRating

    private val _selectedSort = MutableStateFlow(SortOrder.POPULAR)
    val selectedSort: StateFlow<SortOrder> = _selectedSort

    // State: Filtered cocktails
    private val _filteredCocktails = MutableStateFlow<List<SuggestedCocktail>>(emptyList())
    val filteredCocktails: StateFlow<List<SuggestedCocktail>> = _filteredCocktails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Cache of all cocktails loaded
    private var allCocktails: List<SuggestedCocktail> = emptyList()

    enum class SortOrder {
        POPULAR,  // Most viewed/liked (keep original order from API)
        NEWEST,   // Most recently added (keep original order from API)
        TOP_RATED // Highest rating
    }

    /**
     * Filter cocktails by ingredient using TheCocktailDB API
     */
    fun filterByIngredient(ingredient: String) {
        _selectedIngredient.value = ingredient
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.filterByIngredient(ingredient)
                }

                // Convert API response to SuggestedCocktail with ratings
                val cocktails = response.drinks?.mapNotNull { drink ->
                    if (drink.idDrink == null || drink.strDrink == null) {
                        return@mapNotNull null
                    }
                    
                    val rating = try {
                        // Use a default rating based on position (popularity)
                        5.0 - (response.drinks.indexOf(drink) * 0.1).coerceAtMost(2.0)
                    } catch (e: Exception) {
                        3.5  // Default rating if lookup fails
                    }

                    SuggestedCocktail(
                        name = drink.strDrink,
                        rating = rating,
                        category = ingredient,
                        imageUrl = drink.strDrinkThumb,
                        cocktailId = drink.idDrink,
                        isFavorite = false
                    )
                } ?: emptyList()

                allCocktails = cocktails
                applyAllFilters()
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error loading cocktails: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear ingredient filter and reload all
     */
    fun clearIngredientFilter() {
        _selectedIngredient.value = null
        applyAllFilters()
    }

    /**
     * Filter cocktails by category (drink type)
     */
    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        applyAllFilters()
    }

    /**
     * Clear category filter
     */
    fun clearCategoryFilter() {
        _selectedCategory.value = null
        applyAllFilters()
    }

    /**
     * Filter by minimum rating
     */
    fun filterByRating(minRating: Double) {
        _selectedRating.value = minRating
        applyAllFilters()
    }

    /**
     * Clear rating filter
     */
    fun clearRatingFilter() {
        _selectedRating.value = null
        applyAllFilters()
    }

    /**
     * Set sort order
     */
    fun setSortOrder(order: SortOrder) {
        _selectedSort.value = order
        applyAllFilters()
    }

    /**
     * Set initial cocktails (called when page loads with default data)
     */
    fun setInitialCocktails(cocktails: List<SuggestedCocktail>) {
        allCocktails = cocktails
        applyAllFilters()
    }

    /**
     * Apply all active filters to the cocktail list
     */
    private fun applyAllFilters() {
        var result = allCocktails

        // Apply category filter (drink type)
        _selectedCategory.value?.let { category ->
            result = result.filter { it.category == category }
        }

        // Apply rating filter
        _selectedRating.value?.let { minRating ->
            result = result.filter { it.rating >= minRating }
        }

        // Apply sort order
        result = when (_selectedSort.value) {
            SortOrder.POPULAR -> result  // Keep original order (most viewed/liked from API)
            SortOrder.NEWEST -> result   // Keep original order (most recently added from API)
            SortOrder.TOP_RATED -> result.sortedByDescending { it.rating }  // Sort by rating descending
        }

        _filteredCocktails.value = result
    }

    /**
     * Get readable filter description for UI
     */
    fun getFilterDescription(): String {
        val parts = mutableListOf<String>()

        _selectedIngredient.value?.let { parts.add("Ingredient: $it") }
        _selectedRating.value?.let { parts.add("Rating: ${it}+") }
        parts.add("Sort: ${_selectedSort.value}")

        return if (parts.isEmpty()) "No filters" else parts.joinToString(" • ")
    }
}
