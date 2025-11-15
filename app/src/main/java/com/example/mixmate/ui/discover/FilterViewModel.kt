package com.example.mixmate.ui.discover

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mixmate.SuggestedCocktail
import com.example.mixmate.CocktailApiRepository
import com.example.mixmate.CocktailImageProvider
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

    // State: Filter selections
    private val _selectedIngredient = MutableStateFlow<String?>(null)
    val selectedIngredient: StateFlow<String?> = _selectedIngredient

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _selectedRating = MutableStateFlow<Double?>(null)
    val selectedRating: StateFlow<Double?> = _selectedRating

    private val _selectedSort = MutableStateFlow(SortOrder.POPULAR)
    val selectedSort: StateFlow<SortOrder> = _selectedSort

    // Filtered cocktails output
    private val _filteredCocktails = MutableStateFlow<List<SuggestedCocktail>>(emptyList())
    val filteredCocktails: StateFlow<List<SuggestedCocktail>> = _filteredCocktails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Cache of all cocktails loaded
    private var allCocktails: List<SuggestedCocktail> = emptyList()

    // Keep original cocktails for reset
    private var originalCocktails: List<SuggestedCocktail> = emptyList()

    enum class SortOrder {
        POPULAR,  // Most viewed/liked (keep original order from API)
        NEWEST,   // Most recently added (keep original order from API)
        TOP_RATED // Highest rating
    }

    /**
     * Filter cocktails by ingredient using client-side filtering on API Ninjas data
     */
    fun filterByIngredient(ingredient: String) {
        Log.d("FilterViewModel", "Filtering by ingredient: $ingredient")
        _selectedIngredient.value = ingredient
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Load ALL cocktails from API Ninjas if not already loaded
                if (allCocktails.isEmpty() || originalCocktails.isEmpty()) {
                    val apiItems = CocktailApiRepository.fetchCocktails(limit = 100)
                    val enriched = CocktailImageProvider.enrichWithImages(apiItems)
                    allCocktails = enriched
                    if (originalCocktails.isEmpty()) {
                        originalCocktails = enriched
                    }
                }

                Log.d("FilterViewModel", "Total cocktails loaded: ${allCocktails.size}")

                // Apply all filters (including the new ingredient filter)
                applyAllFilters()
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("FilterViewModel", "Error loading cocktails", e)
                _error.value = "Error loading cocktails: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear ingredient filter and restore original cocktails
     */
    fun clearIngredientFilter() {
        Log.d("FilterViewModel", "Clearing ingredient filter, restoring original cocktails")
        _selectedIngredient.value = null
        // Restore original cocktails when ingredient filter is cleared
        if (originalCocktails.isNotEmpty()) {
            allCocktails = originalCocktails
        }
        applyAllFilters()
    }

    /**
     * Filter cocktails by category (drink type)
     */
    fun filterByCategory(category: String) {
        Log.d("FilterViewModel", "Filtering by category: $category")
        _selectedCategory.value = category
        applyAllFilters()
    }

    /**
     * Clear category filter
     */
    fun clearCategoryFilter() {
        Log.d("FilterViewModel", "Clearing category filter")
        _selectedCategory.value = null
        applyAllFilters()
    }

    /**
     * Filter by minimum rating
     */
    fun filterByRating(minRating: Double) {
        Log.d("FilterViewModel", "Filtering by rating: $minRating")
        _selectedRating.value = minRating
        applyAllFilters()
    }

    /**
     * Clear rating filter
     */
    fun clearRatingFilter() {
        Log.d("FilterViewModel", "Clearing rating filter")
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
        originalCocktails = cocktails // Save original for reset
        applyAllFilters()
    }

    /**
     * Apply all active filters to the cocktail list
     * Now supports multiple filters simultaneously with client-side filtering
     */
    private fun applyAllFilters() {
        Log.d("FilterViewModel", "Applying filters. Total cocktails: ${allCocktails.size}")
        Log.d("FilterViewModel", "Selected ingredient: ${_selectedIngredient.value}")
        Log.d("FilterViewModel", "Selected category: ${_selectedCategory.value}")
        Log.d("FilterViewModel", "Selected rating: ${_selectedRating.value}")

        var result = allCocktails

        // Apply ingredient filter (client-side on ingredients list)
        _selectedIngredient.value?.let { ingredient ->
            Log.d("FilterViewModel", "Filtering by ingredient: $ingredient")
            result = result.filter { cocktail ->
                // Check if ingredient appears in the cocktail's name
                val nameMatch = cocktail.name.contains(ingredient, ignoreCase = true)

                // Check if ingredient appears in actual ingredient list (from API Ninjas)
                val ingredientListMatch = cocktail.ingredients?.any { ing ->
                    ing.contains(ingredient, ignoreCase = true)
                } ?: false

                val matches = nameMatch || ingredientListMatch
                if (matches) {
                    Log.d("FilterViewModel", "Match: ${cocktail.name} (nameMatch=$nameMatch, ingredientMatch=$ingredientListMatch)")
                }
                matches
            }
            Log.d("FilterViewModel", "After ingredient filter: ${result.size} cocktails")
        }

        // Apply category filter (drink type) - works with ingredient filter
        _selectedCategory.value?.let { category ->
            Log.d("FilterViewModel", "Filtering by category: $category")
            result = result.filter {
                val matches = it.category.equals(category, ignoreCase = true) ||
                        it.category.contains(category, ignoreCase = true)
                if (matches) {
                    Log.d("FilterViewModel", "Match found: ${it.name} with category ${it.category}")
                }
                matches
            }
            Log.d("FilterViewModel", "After category filter: ${result.size} cocktails")
        }

        // Apply rating filter - works with both ingredient and category filters
        _selectedRating.value?.let { minRating ->
            result = result.filter { it.rating >= minRating }
            Log.d("FilterViewModel", "After rating filter: ${result.size} cocktails")
        }

        // Apply sort order
        result = when (_selectedSort.value) {
            SortOrder.POPULAR -> result  // Keep original order (most viewed/liked from API)
            SortOrder.NEWEST -> result   // Keep original order (most recently added from API)
            SortOrder.TOP_RATED -> result.sortedByDescending { it.rating }  // Sort by rating descending
        }

        Log.d("FilterViewModel", "Final filtered count: ${result.size}")
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
