package com.nfar.allergydetective.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.normalizeForSearch
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.meal.IngredientCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DishEditorState(
    /** Id of the dish being edited, null for a new dish. */
    val editingId: Long? = null,
    /** Portions of an edited dish are kept; new ingredients get M. */
    val portions: Map<Long, Portion> = emptyMap(),
    val name: String = "",
    val ingredients: List<IngredientInfo> = emptyList(),
    /** Raw or cooked per ingredient id, for the ingredients in [needsPreparation]. */
    val preparations: Map<Long, Preparation> = emptyMap(),
    val needsPreparation: Set<Long> = emptySet(),
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    /** Saved dishes whose name matches the search; the dish being edited is left out. */
    val dishResults: List<FavoriteInfo> = emptyList(),
    /** A dish with the same name exists; saving replaces it after confirmation. */
    val sameNameDish: FavoriteInfo? = null,
    val saving: Boolean = false,
    /** Set after saving, the dialog closes then. */
    val saved: Boolean = false,
) {
    val missingPreparation: Boolean get() = ingredients.any { it.id in needsPreparation && it.id !in preparations }
    val canSave: Boolean get() = name.isNotBlank() && ingredients.isNotEmpty() && !missingPreparation && !saving
}

/**
 * "+ New dish" in the favorites tab (self-test 18.11.): e.g. lentil soup = lentils, tomatoes,
 * carrots (cooked), spices. Saved as a favorite, so it can be logged with one tap.
 */
class DishEditorViewModel(
    private val meals: MealRepository,
    private val names: DisplayNameResolver,
    private val language: LanguageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(DishEditorState())
    val state: StateFlow<DishEditorState> = _state.asStateFlow()

    private var catalog = IngredientCatalog.EMPTY
    private var favorites: List<FavoriteInfo> = emptyList()

    init {
        viewModelScope.launch { reload() }
    }

    private suspend fun reload() {
        catalog = IngredientCatalog.load(meals, names, language)
        favorites = meals.favorites()
        val needs = meals.ingredientIdsNeedingPreparation()
        _state.update { it.copy(needsPreparation = needs) }
    }

    /**
     * Opens the form: empty for a new dish, or filled with [dish] to edit it (self-test 18.11.:
     * a forgotten ingredient). Reloads the catalog for new custom ingredients.
     */
    fun start(dish: FavoriteInfo? = null) {
        _state.value = DishEditorState(needsPreparation = _state.value.needsPreparation)
        viewModelScope.launch {
            reload()
            if (dish == null) return@launch
            _state.update {
                it.copy(
                    editingId = dish.id,
                    name = dish.name,
                    ingredients = dish.items.mapNotNull { item -> catalog.byId[item.ingredientId] },
                    portions = dish.items.associate { item -> item.ingredientId to item.portion },
                    preparations = dish.items.mapNotNull { item -> item.preparation?.let { p -> item.ingredientId to p } }.toMap(),
                )
            }
        }
    }

    fun onNameChange(name: String) = _state.update {
        val normalized = normalizeForSearch(name)
        // The dish being edited may keep its own name.
        val other = favorites.firstOrNull { f -> f.id != it.editingId && normalized.isNotEmpty() && normalizeForSearch(f.name) == normalized }
        it.copy(name = name, sameNameDish = other)
    }

    fun onQueryChange(query: String) = _state.update {
        val normalized = normalizeForSearch(query)
        val dishes = if (normalized.length < 2) emptyList() else favorites.filter { f -> f.id != it.editingId && normalizeForSearch(f.name).contains(normalized) }
        it.copy(query = query, results = catalog.search(query), dishResults = dishes.take(3))
    }

    /** Adds every ingredient of another dish (e.g. "Gewürzmischung" into a soup), keeping its portions and raw / cooked. */
    fun onAddDish(dish: FavoriteInfo) = _state.update { state ->
        val new = dish.items.filter { item -> state.ingredients.none { it.id == item.ingredientId } }
        state.copy(
            ingredients = state.ingredients + new.mapNotNull { catalog.byId[it.ingredientId] },
            portions = state.portions + new.associate { it.ingredientId to it.portion },
            preparations = state.preparations + new.mapNotNull { item -> item.preparation?.let { item.ingredientId to it } },
            query = "",
            results = emptyList(),
            dishResults = emptyList(),
        )
    }

    fun onAdd(ingredient: IngredientInfo) = _state.update {
        if (ingredient in it.ingredients) it else it.copy(ingredients = it.ingredients + ingredient, query = "", results = emptyList(), dishResults = emptyList())
    }

    fun onRemove(ingredient: IngredientInfo) = _state.update {
        it.copy(ingredients = it.ingredients - ingredient, preparations = it.preparations - ingredient.id)
    }

    fun onPreparationChange(ingredientId: Long, preparation: Preparation) = _state.update {
        it.copy(preparations = it.preparations + (ingredientId to preparation))
    }

    /** A custom ingredient was created from the search text: make it findable and add it. */
    fun onIngredientCreated(ingredient: IngredientInfo) {
        viewModelScope.launch {
            reload()
            onAdd(catalog.byId[ingredient.id] ?: ingredient)
        }
    }

    /** Saves the dish; a dish with the same name is replaced (the user confirmed in the dialog). */
    fun onSave() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            current.sameNameDish?.let { meals.deleteFavorite(it.id) }
            val items = current.ingredients.map { MealItem(it.id, current.portions[it.id] ?: Portion.MEDIUM, current.preparations[it.id]) }
            val editingId = current.editingId
            if (editingId != null) meals.updateFavorite(editingId, current.name.trim(), items)
            else meals.saveFavorite(current.name.trim(), items)
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}
