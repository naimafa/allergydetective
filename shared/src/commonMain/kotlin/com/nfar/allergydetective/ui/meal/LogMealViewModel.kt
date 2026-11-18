package com.nfar.allergydetective.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.IngredientSearch
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.SearchEntry
import com.nfar.allergydetective.domain.diary.VoiceMatcher
import com.nfar.allergydetective.domain.diary.normalizeForSearch
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.domain.time.timeOf
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.common.ExactTime
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.timeOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SelectedIngredient(
    val ingredient: IngredientInfo,
    val portion: Portion = Portion.MEDIUM,
    /** Raw or cooked; must be chosen for ingredients in [LogMealState.needsPreparation]. */
    val preparation: Preparation? = null,
)

data class LogMealState(
    val loading: Boolean = true,
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    val recent: List<IngredientInfo> = emptyList(),
    val favorites: List<FavoriteInfo> = emptyList(),
    val selected: List<SelectedIngredient> = emptyList(),
    /** Breakfast, lunch, dinner or a snack (self-test 18.11.); preselected by the time of day. */
    val slot: MealSlot = MealSlot.SNACK,
    /** Picked in the time dialog, overrides [slot]. */
    val exactTime: ExactTime? = null,
    /** Favorite the selection came from, stored as origin of the meal (TRD 3.2). */
    val favoriteId: Long? = null,
    val saveAsFavorite: Boolean = false,
    val favoriteName: String = "",
    val saving: Boolean = false,
    /** The search text is exactly the name of an existing ingredient: no "create new" then. */
    val nameExists: Boolean = false,
    /** Set after saving, so the screen can offer undo. Cleared with [LogMealViewModel.onSavedShown]. */
    val savedMealId: Long? = null,
    private val allergyIngredientIds: Set<Long> = emptySet(),
    /** Ingredients with a heat-labile trigger (birch pollen): raw or cooked is mandatory. */
    val needsPreparation: Set<Long> = emptySet(),
) {
    val canSave: Boolean get() =
        selected.isNotEmpty() && !saving && !missingPreparation && (!saveAsFavorite || favoriteName.isNotBlank())

    val missingPreparation: Boolean get() = selected.any { it.ingredient.id in needsPreparation && it.preparation == null }

    /** UX 3.1: a gentle hint, saving stays possible. */
    val containsKnownAllergy: Boolean get() = selected.any { it.ingredient.id in allergyIngredientIds }

    internal fun withAllergies(ids: Set<Long>) = copy(allergyIngredientIds = ids)
}

/** Food log (UX 3.1): time, favorites, recent ingredients, search, portions, save with undo. */
class LogMealViewModel(
    private val repository: MealRepository,
    private val clock: AppClock,
    private val names: DisplayNameResolver,
    private val language: LanguageProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(LogMealState())
    val state: StateFlow<LogMealState> = _state.asStateFlow()

    private var ingredientsById: Map<Long, IngredientInfo> = emptyMap()
    private var search = IngredientSearch(emptyList())

    /** Normalized display names of all ingredients, to avoid creating the same name twice. */
    private var knownNames: Set<String> = emptySet()

    private fun suggestedSlot(): MealSlot = clock.localTime(clock.nowMillis()).let { (h, m) -> MealSlot.suggestedAt(h, m) }

    init {
        _state.update { it.copy(slot = suggestedSlot()) }
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val ingredients = repository.ingredients()
        ingredientsById = ingredients.associateBy { it.id }
        val synonyms = repository.synonyms(language.language())
        search = IngredientSearch(
            ingredients.map { SearchEntry(it.id, displayName(it), synonyms[it.id].orEmpty()) },
        )
        knownNames = ingredients.map { normalizeForSearch(displayName(it)) }.toSet()
        val allergies = repository.ingredientIdsWithKnownAllergy()
        val needsPreparation = repository.ingredientIdsNeedingPreparation()
        val recent = recentIngredients()
        val favorites = repository.favorites()
        _state.update {
            it.copy(loading = false, recent = recent, favorites = favorites, needsPreparation = needsPreparation).withAllergies(allergies)
        }
    }

    /** A custom ingredient was created: make it searchable and select it. */
    fun onCustomIngredientCreated(ingredient: IngredientInfo) {
        viewModelScope.launch {
            load()
            onToggleIngredient(ingredientsById[ingredient.id] ?: ingredient)
        }
    }

    private suspend fun displayName(ingredient: IngredientInfo): String =
        ingredient.key?.let { names.name(NamePrefix.INGREDIENT, it) } ?: ingredient.customName.orEmpty()

    private suspend fun recentIngredients(): List<IngredientInfo> =
        repository.recentIngredientIds(RECENT_LIMIT).mapNotNull { ingredientsById[it] }
            // Self-test 18.11.: a custom and a seed ingredient can share a name; show it once.
            .distinctBy { normalizeForSearch(displayName(it)) }

    fun onQueryChange(query: String) {
        _state.update {
            it.copy(
                query = query,
                results = search.search(query).mapNotNull(ingredientsById::get),
                nameExists = normalizeForSearch(query) in knownNames,
            )
        }
    }

    fun onSlotChange(slot: MealSlot) {
        _state.update { it.copy(slot = slot, exactTime = null) }
    }

    fun onExactTimeChange(time: ExactTime) {
        _state.update { it.copy(exactTime = time) }
    }

    /** Adds the ingredient, or removes it when it is already selected. Clears the search. */
    fun onToggleIngredient(ingredient: IngredientInfo) {
        _state.update { state ->
            val selected = if (state.selected.any { it.ingredient.id == ingredient.id }) {
                state.selected.filterNot { it.ingredient.id == ingredient.id }
            } else {
                state.selected + SelectedIngredient(ingredient)
            }
            state.copy(
                selected = selected,
                query = "",
                results = emptyList(),
                favoriteId = state.favoriteId.takeIf { selected.isNotEmpty() },
            )
        }
    }

    /**
     * Spoken text (UX 3.2): matches go to the selection, unknown words into the search field,
     * where "create new ingredient" is offered.
     */
    fun onVoiceResult(text: String) {
        val current = _state.value
        val match = VoiceMatcher(search, current.favorites).match(text)
        match.favoriteIds.mapNotNull { id -> current.favorites.firstOrNull { it.id == id } }.forEach(::onFavoriteClick)
        val selectedIds = _state.value.selected.map { it.ingredient.id }.toSet()
        match.ingredientIds
            .filter { it !in selectedIds }
            .mapNotNull(ingredientsById::get)
            .forEach(::onToggleIngredient)
        if (match.unmatched.isNotEmpty()) onQueryChange(match.unmatched.joinToString(" "))
    }

    /** Adds the ingredients of a favorite with their default portions (UX 3.1). */
    fun onFavoriteClick(favorite: FavoriteInfo) {
        _state.update { state ->
            val known = state.selected.map { it.ingredient.id }.toSet()
            val added = favorite.items
                .filter { it.ingredientId !in known }
                .mapNotNull { item -> ingredientsById[item.ingredientId]?.let { SelectedIngredient(it, item.portion, item.preparation) } }
            state.copy(selected = state.selected + added, favoriteId = favorite.id)
        }
    }

    fun onPortionChange(ingredientId: Long, portion: Portion) {
        _state.update { state ->
            state.copy(selected = state.selected.map { if (it.ingredient.id == ingredientId) it.copy(portion = portion) else it })
        }
    }

    fun onPreparationChange(ingredientId: Long, preparation: Preparation) {
        _state.update { state ->
            state.copy(selected = state.selected.map { if (it.ingredient.id == ingredientId) it.copy(preparation = preparation) else it })
        }
    }

    fun onSaveAsFavoriteChange(enabled: Boolean) {
        _state.update { it.copy(saveAsFavorite = enabled) }
    }

    fun onFavoriteNameChange(name: String) {
        _state.update { it.copy(favoriteName = name) }
    }

    fun onSave() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val items = current.selected.map { MealItem(it.ingredient.id, it.portion, if (it.ingredient.id in current.needsPreparation) it.preparation else null) }
            val favoriteId = if (current.saveAsFavorite) {
                repository.saveFavorite(current.favoriteName.trim(), items)
            } else {
                current.favoriteId
            }
            val eatenAt = current.exactTime?.let { clock.timeOf(it, 0) } ?: clock.timeOf(current.slot)
            val mealId = repository.saveMeal(
                eatenAt = eatenAt,
                tzOffsetMin = clock.offsetMinAt(eatenAt),
                items = items,
                favoriteId = favoriteId,
            )
            val recent = recentIngredients()
            val favorites = repository.favorites()
            // Back to a fresh form; allergies and catalog stay as loaded.
            _state.update {
                it.copy(
                    query = "",
                    results = emptyList(),
                    selected = emptyList(),
                    slot = suggestedSlot(),
                    exactTime = null,
                    favoriteId = null,
                    saveAsFavorite = false,
                    favoriteName = "",
                    saving = false,
                    savedMealId = mealId,
                    recent = recent,
                    favorites = favorites,
                )
            }
        }
    }

    fun onUndo(mealId: Long) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
            val recent = recentIngredients()
            _state.update { it.copy(recent = recent) }
        }
    }

    fun onSavedShown() {
        _state.update { it.copy(savedMealId = null) }
    }

    companion object {
        private const val RECENT_LIMIT = 12
    }
}
