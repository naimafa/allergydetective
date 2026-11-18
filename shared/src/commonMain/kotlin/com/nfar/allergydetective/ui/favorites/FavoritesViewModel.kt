package com.nfar.allergydetective.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesState(
    val loading: Boolean = true,
    val favorites: List<FavoriteInfo> = emptyList(),
    val ingredients: Map<Long, IngredientInfo> = emptyMap(),
)

/** Tab "Favoriten": shows the saved meals and lets the user delete them. */
class FavoritesViewModel(private val meals: MealRepository) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val ingredients = meals.ingredients().associateBy { it.id }
            _state.value = FavoritesState(loading = false, favorites = meals.favorites(), ingredients = ingredients)
        }
    }

    /** Logged meals keep their items, only the favorite disappears. */
    fun onDelete(favoriteId: Long) {
        viewModelScope.launch {
            meals.deleteFavorite(favoriteId)
            refresh()
        }
    }
}
