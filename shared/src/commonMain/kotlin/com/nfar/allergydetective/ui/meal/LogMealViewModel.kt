package com.nfar.allergydetective.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.IngredientSearch
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.SearchEntry
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.common.ExactTime
import com.nfar.allergydetective.ui.common.timeOf
import com.nfar.allergydetective.ui.common.NamePrefix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SelectedIngredient(val ingredient: IngredientInfo, val portion: Portion = Portion.MEDIUM)

data class LogMealState(
    val loading: Boolean = true,
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    val recent: List<IngredientInfo> = emptyList(),
    val selected: List<SelectedIngredient> = emptyList(),
    val hoursAgo: Int = 0,
    /** Picked in the time dialog, overrides [hoursAgo]. */
    val exactTime: ExactTime? = null,
    val saving: Boolean = false,
    /** Set after saving, so the screen can offer undo. Cleared with [LogMealViewModel.onSavedShown]. */
    val savedMealId: Long? = null,
    private val allergyIngredientIds: Set<Long> = emptySet(),
) {
    val canSave: Boolean get() = selected.isNotEmpty() && !saving

    /** UX 3.1: a gentle hint, saving stays possible. */
    val containsKnownAllergy: Boolean get() = selected.any { it.ingredient.id in allergyIngredientIds }

    internal fun withAllergies(ids: Set<Long>) = copy(allergyIngredientIds = ids)
}

/** Food log (UX 3.1): time, recent ingredients, search, portions, save with undo. */
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

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val ingredients = repository.ingredients()
        ingredientsById = ingredients.associateBy { it.id }
        val synonyms = repository.synonyms(language.language())
        search = IngredientSearch(
            ingredients.map { SearchEntry(it.id, displayName(it), synonyms[it.id].orEmpty()) },
        )
        val allergies = repository.ingredientIdsWithKnownAllergy()
        val recent = repository.recentIngredientIds(RECENT_LIMIT).mapNotNull { ingredientsById[it] }
        _state.update { it.copy(loading = false, recent = recent).withAllergies(allergies) }
    }

    private suspend fun displayName(ingredient: IngredientInfo): String =
        ingredient.key?.let { names.name(NamePrefix.INGREDIENT, it) } ?: ingredient.customName.orEmpty()

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query, results = search.search(query).mapNotNull(ingredientsById::get)) }
    }

    fun onHoursAgoChange(hours: Int) {
        _state.update { it.copy(hoursAgo = hours, exactTime = null) }
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
            state.copy(selected = selected, query = "", results = emptyList())
        }
    }

    fun onPortionChange(ingredientId: Long, portion: Portion) {
        _state.update { state ->
            state.copy(selected = state.selected.map { if (it.ingredient.id == ingredientId) it.copy(portion = portion) else it })
        }
    }

    fun onSave() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val eatenAt = clock.timeOf(current.exactTime, current.hoursAgo * MINUTES_PER_HOUR)
            val mealId = repository.saveMeal(
                eatenAt = eatenAt,
                tzOffsetMin = clock.offsetMinAt(eatenAt),
                items = current.selected.map { MealItem(it.ingredient.id, it.portion) },
            )
            val recent = repository.recentIngredientIds(RECENT_LIMIT).mapNotNull { ingredientsById[it] }
            _state.update {
                it.copy(saving = false, selected = emptyList(), hoursAgo = 0, exactTime = null, savedMealId = mealId, recent = recent)
            }
        }
    }

    fun onUndo(mealId: Long) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
            val recent = repository.recentIngredientIds(RECENT_LIMIT).mapNotNull { ingredientsById[it] }
            _state.update { it.copy(recent = recent) }
        }
    }

    fun onSavedShown() {
        _state.update { it.copy(savedMealId = null) }
    }

    companion object {
        /** Time chips: now, 1, 2, 3 and 5 hours ago (UX 3.1). */
        val HOURS_AGO_OPTIONS = listOf(0, 1, 2, 3, 5)
        private const val RECENT_LIMIT = 12
        private const val MINUTES_PER_HOUR = 60
    }
}
