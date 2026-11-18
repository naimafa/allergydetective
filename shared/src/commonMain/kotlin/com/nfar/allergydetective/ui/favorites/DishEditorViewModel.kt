package com.nfar.allergydetective.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.meal.IngredientCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DishEditorState(
    val name: String = "",
    val ingredients: List<IngredientInfo> = emptyList(),
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    val saving: Boolean = false,
    /** Set after saving, the dialog closes then. */
    val saved: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank() && ingredients.isNotEmpty() && !saving
}

/**
 * "+ New dish" in the favorites tab (self-test 18.11.): e.g. lentil soup = lentils, tomatoes,
 * carrots, spices. Saved as a favorite, so it can be logged with one tap.
 */
class DishEditorViewModel(
    private val meals: MealRepository,
    private val names: DisplayNameResolver,
    private val language: LanguageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(DishEditorState())
    val state: StateFlow<DishEditorState> = _state.asStateFlow()

    private var catalog = IngredientCatalog.EMPTY

    init {
        viewModelScope.launch { catalog = IngredientCatalog.load(meals, names, language) }
    }

    /** Fresh form each time the dialog opens; reloads the catalog for new custom ingredients. */
    fun start() {
        _state.value = DishEditorState()
        viewModelScope.launch { catalog = IngredientCatalog.load(meals, names, language) }
    }

    fun onNameChange(name: String) = _state.update { it.copy(name = name) }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query, results = catalog.search(query)) }

    fun onAdd(ingredient: IngredientInfo) = _state.update {
        if (ingredient in it.ingredients) it else it.copy(ingredients = it.ingredients + ingredient, query = "", results = emptyList())
    }

    fun onRemove(ingredient: IngredientInfo) = _state.update { it.copy(ingredients = it.ingredients - ingredient) }

    fun onSave() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            meals.saveFavorite(current.name.trim(), current.ingredients.map { MealItem(it.id, Portion.MEDIUM) })
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}
