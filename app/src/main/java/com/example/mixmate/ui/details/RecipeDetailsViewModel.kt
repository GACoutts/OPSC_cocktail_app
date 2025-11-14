package com.example.mixmate.ui.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mixmate.data.local.FavoriteEntity
import com.example.mixmate.data.remote.formatIngredients
import com.example.mixmate.data.repo.CocktailRepository
import com.example.mixmate.data.repo.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RecipeDetailsUi(
    val loading: Boolean = true,
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val ingredients: String = "",
    val instructions: String = "",
    val isFavorited: Boolean = false,
    val error: String? = null
)

class RecipeDetailsViewModel(
    private val userId: String,
    private val cocktails: CocktailRepository = CocktailRepository(),
    private val favs: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(RecipeDetailsUi())
    val ui: StateFlow<RecipeDetailsUi> = _ui

    /** Try Room first (offline), else fetch from API */
    fun load(cocktailId: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            Log.d("RecipeDetailsVM", "Loading cocktail with ID: $cocktailId")

            try {
                // First check if it's already favorited (in Room)
                val saved = favs.getById(cocktailId, userId)
                if (saved != null) {
                    Log.d("RecipeDetailsVM", "Found in favorites: ${saved.name}")
                    _ui.value = RecipeDetailsUi(
                        loading = false,
                        id = saved.cocktailId,
                        name = saved.name,
                        imageUrl = saved.imageUrl,
                        ingredients = saved.ingredients,
                        instructions = saved.instructions,
                        isFavorited = true
                    )
                    return@launch
                }

                // Fetch from API
                Log.d("RecipeDetailsVM", "Fetching from API...")
                val drink = cocktails.getDrinkById(cocktailId)

                if (drink == null) {
                    Log.e("RecipeDetailsVM", "API returned null for ID: $cocktailId")
                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = "Recipe not found. Please try another cocktail."
                    )
                } else {
                    Log.d("RecipeDetailsVM", "Successfully loaded: ${drink.strDrink}")
                    val ingredients = formatIngredients(drink)
                    val instructions = drink.strInstructions.orEmpty()

                    if (ingredients.isBlank() && instructions.isBlank()) {
                        Log.w("RecipeDetailsVM", "Recipe has no details")
                        _ui.value = RecipeDetailsUi(
                            loading = false,
                            id = drink.idDrink.orEmpty(),
                            name = drink.strDrink.orEmpty(),
                            imageUrl = drink.strDrinkThumb.orEmpty(),
                            ingredients = "No ingredients available",
                            instructions = "No instructions available",
                            isFavorited = false,
                            error = "Limited information available for this recipe"
                        )
                    } else {
                        _ui.value = RecipeDetailsUi(
                            loading = false,
                            id = drink.idDrink.orEmpty(),
                            name = drink.strDrink.orEmpty(),
                            imageUrl = drink.strDrinkThumb.orEmpty(),
                            ingredients = ingredients,
                            instructions = instructions,
                            isFavorited = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailsVM", "Error loading recipe: ${e.message}", e)
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = "Failed to load recipe: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun toggleFavorite() {
        val s = _ui.value
        if (s.id.isBlank()) return
        viewModelScope.launch {
            val exists = favs.getById(s.id, userId)
            if (exists == null) {
                favs.upsert(
                    FavoriteEntity(
                        cocktailId = s.id,
                        name = s.name,
                        imageUrl = s.imageUrl,
                        ingredients = s.ingredients,
                        instructions = s.instructions,
                        userId = userId
                    )
                )
                _ui.value = s.copy(isFavorited = true)
            } else {
                favs.deleteById(s.id, userId)
                _ui.value = s.copy(isFavorited = false)
            }
        }
    }
}
