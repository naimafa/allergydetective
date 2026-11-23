package com.nfar.allergydetective.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.matches
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.common.NamePrefix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesState(
    val loading: Boolean = true,
    val favorites: List<FavoriteInfo> = emptyList(),
    val ingredients: Map<Long, IngredientInfo> = emptyMap(),
    /** "Dish or ingredient …" (self-test 19.11.). */
    val query: String = "",
    private val ingredientNames: Map<Long, String> = emptyMap(),
) {
    /** Dishes whose name or ingredients contain [query]. */
    val visibleFavorites: List<FavoriteInfo> get() = favorites.filter { it.matches(query, ingredientNames) }

    internal fun withNames(names: Map<Long, String>) = copy(ingredientNames = names)
}

/** Tab "Favoriten": shows the saved dishes, searches and deletes them. */
class FavoritesViewModel(
    private val meals: MealRepository,
    private val names: DisplayNameResolver,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val ingredients = meals.ingredients().associateBy { it.id }
            val ingredientNames = ingredients.mapValues { (_, it) ->
                it.key?.let { key -> names.name(NamePrefix.INGREDIENT, key) } ?: it.customName.orEmpty()
            }
            val favorites = meals.favorites()
            _state.update {
                it.copy(loading = false, favorites = favorites, ingredients = ingredients).withNames(ingredientNames)
            }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    /**
     * Dragging (user note 21.11.2026): the dish [from] takes the place of [to] while the finger moves; the order
     * is saved when the finger lifts ([onMoveDone]). Only without a search, so the full list is reordered.
     */
    fun onMove(from: Long, to: Long) {
        _state.update { state ->
            val list = state.favorites.toMutableList()
            val fromIndex = list.indexOfFirst { it.id == from }
            val toIndex = list.indexOfFirst { it.id == to }
            if (fromIndex < 0 || toIndex < 0 || state.query.isNotBlank()) return@update state
            list.add(toIndex, list.removeAt(fromIndex))
            state.copy(favorites = list)
        }
    }

    fun onMoveDone() {
        val ids = _state.value.favorites.map { it.id }
        viewModelScope.launch { meals.reorderFavorites(ids) }
    }

    /** Logged meals keep their items, only the favorite disappears. */
    fun onDelete(favoriteId: Long) {
        viewModelScope.launch {
            meals.deleteFavorite(favoriteId)
            refresh()
        }
    }
}
