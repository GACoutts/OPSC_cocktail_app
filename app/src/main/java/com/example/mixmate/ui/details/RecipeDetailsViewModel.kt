package com.example.mixmate.ui.details

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

            val saved = favs.getById(cocktailId, userId)
            if (saved != null) {
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

            runCatching { cocktails.getDrinkById(cocktailId) }
                .onSuccess { drink ->
                    if (drink == null) {
                        _ui.value = _ui.value.copy(loading = false, error = "Not found")
                    } else {
                        _ui.value = RecipeDetailsUi(
                            loading = false,
                            id = drink.idDrink.orEmpty(),
                            name = drink.strDrink.orEmpty(),
                            imageUrl = drink.strDrinkThumb.orEmpty(),
                            ingredients = formatIngredients(drink),
                            instructions = drink.strInstructions.orEmpty(),
                            isFavorited = false
                        )
                    }
                }
                .onFailure { e ->
                    _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Error")
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
