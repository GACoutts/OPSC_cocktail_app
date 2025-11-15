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

    private var initialName: String = ""
    private var initialImage: String = ""

    fun setInitial(name: String, image: String) {
        initialName = name
        initialImage = image
        _ui.value = RecipeDetailsUi(
            loading = false,
            id = "",
            name = name,
            imageUrl = image,
            ingredients = "Ingredients not available",
            instructions = "Instructions not available",
            isFavorited = false,
            error = null
        )
    }

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
                    // Still show whatever we can - don't block the UI
                    _ui.value = _ui.value.copy(
                        loading = false,
                        name = initialName.ifBlank { "Cocktail #$cocktailId" },
                        imageUrl = initialImage,
                        ingredients = "Unable to load ingredients",
                        instructions = "Unable to load instructions",
                        error = null // Don't show error, just display what we have
                    )
                }
                else {
                    Log.d("RecipeDetailsVM", "Successfully loaded: ${drink.strDrink}")
                    val ingredients = formatIngredients(drink)
                    val instructions = drink.strInstructions.orEmpty()

                    // Always show the drink, even if some details are missing
                    _ui.value = RecipeDetailsUi(
                        loading = false,
                        id = drink.idDrink.orEmpty(),
                        name = drink.strDrink ?: initialName.ifBlank { "Unknown Cocktail" },
                        imageUrl = (drink.strDrinkThumb ?: "").ifBlank { initialImage },
                        ingredients = if (ingredients.isNotBlank()) ingredients else "No ingredients available",
                        instructions = if (instructions.isNotBlank()) instructions else "No instructions available",
                        isFavorited = false
                    )
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailsVM", "Error loading recipe: ${e.message}", e)
                // Don't completely fail - show a basic view
                _ui.value = _ui.value.copy(
                    loading = false,
                    name = initialName.ifBlank { "Cocktail #$cocktailId" },
                    imageUrl = initialImage,
                    ingredients = "Unable to load details at this time",
                    instructions = "Please check your internet connection and try again",
                    error = null // Show content instead of error
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
