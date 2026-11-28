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
import com.nfar.allergydetective.ui.meal.LogMealViewModel
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
    /** The search text is already an ingredient or dish: no "create as new ingredient". */
    val nameExists: Boolean = false,
    /** A dish with the same name exists; saving replaces it after confirmation. */
    val sameNameDish: FavoriteInfo? = null,
    val saving: Boolean = false,
    /** Number of logged meals with this dish while the user is asked whether they change too; null = no question. */
    val askEarlierMeals: Int? = null,
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
        // User 25.11.2026: no "create as new ingredient" for the name of an ingredient or a dish that exists.
        val taken = normalized.isNotBlank() && (
            catalog.bestMatch(query)?.second == 0 || favorites.any { f -> normalizeForSearch(f.name) == normalized }
            )
        it.copy(query = query, results = catalog.search(query), dishResults = dishes.take(3), nameExists = taken)
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

    /** User 25.11.2026: a dish can be the gluten-free version ("meine glutenfreie Pizza"); normal by default. */
    fun onGlutenFreeChange(ingredientId: Long, glutenFree: Boolean) = _state.update {
        it.copy(preparations = if (glutenFree) it.preparations + (ingredientId to Preparation.GLUTEN_FREE) else it.preparations - ingredientId)
    }

    /** A custom ingredient was created from the search text: make it findable and add it. */
    fun onIngredientCreated(ingredient: IngredientInfo) {
        viewModelScope.launch {
            reload()
            onAdd(catalog.byId[ingredient.id] ?: ingredient)
        }
    }

    /**
     * Saves the dish; a dish with the same name is replaced (the user confirmed in the dialog). When ingredients of a dish
     * that was logged before were added or removed, the user is asked first whether the earlier meals change too
     * (user 28.11.2026); the answer comes in [onEarlierMealsChoice].
     */
    fun onSave() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val editingId = current.editingId
            if (editingId != null && dishChange(current, editingId) != null) {
                val count = meals.mealIdsWithDish(editingId).size
                if (count > 0) {
                    _state.update { it.copy(saving = false, askEarlierMeals = count) }
                    return@launch
                }
            }
            persist(current, applyToEarlier = false)
        }
    }

    /** The answer of the question: [applyToEarlier] false = only meals from now on, true = the logged ones change too. */
    fun onEarlierMealsChoice(applyToEarlier: Boolean) {
        val current = _state.value
        _state.update { it.copy(askEarlierMeals = null, saving = true) }
        viewModelScope.launch { persist(current, applyToEarlier) }
    }

    /** Back to the form without saving. */
    fun onEarlierMealsDismiss() = _state.update { it.copy(askEarlierMeals = null) }

    private class DishChange(val added: List<MealItem>, val removed: Set<Long>)

    /** What differs from the saved dish: ingredients added and removed; null when none (name or portions only). */
    private suspend fun dishChange(current: DishEditorState, editingId: Long): DishChange? {
        val old = meals.favorites().firstOrNull { it.id == editingId }?.items.orEmpty()
        val oldIds = old.map { it.ingredientId }.toSet()
        val items = newItems(current)
        val added = items.filter { it.ingredientId !in oldIds }
        val removed = oldIds - items.map { it.ingredientId }.toSet()
        return if (added.isEmpty() && removed.isEmpty()) null else DishChange(added, removed)
    }

    private fun newItems(current: DishEditorState) =
        current.ingredients.map { MealItem(it.id, current.portions[it.id] ?: Portion.MEDIUM, current.preparations[it.id]) }

    private suspend fun persist(current: DishEditorState, applyToEarlier: Boolean) {
        current.sameNameDish?.let { meals.deleteFavorite(it.id) }
        val items = newItems(current)
        val editingId = current.editingId
        // The change is worked out before the saved dish is replaced.
        val change = if (editingId != null && applyToEarlier) dishChange(current, editingId) else null
        if (editingId != null) meals.updateFavorite(editingId, current.name.trim(), items)
        else meals.saveFavorite(current.name.trim(), items)
        if (editingId != null && change != null) {
            val minor = catalog.byId.values.filter { it.categoryId in LogMealViewModel.MINOR_CATEGORY_IDS }.map { it.id }.toSet()
            meals.applyDishChange(editingId, change.added, change.removed, minor)
        }
        _state.update { it.copy(saving = false, saved = true) }
    }
}
