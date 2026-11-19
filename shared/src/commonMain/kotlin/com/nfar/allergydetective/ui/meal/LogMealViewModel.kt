package com.nfar.allergydetective.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.IngredientSearch
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.SearchEntry
import com.nfar.allergydetective.domain.diary.VoiceMatcher
import com.nfar.allergydetective.domain.diary.matches
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

/**
 * Food log (UX 3.1): time, favorites, recent ingredients, search, portions, save with undo.
 * Also edits a logged meal from the history and adds to an existing meal of the same slot (self-test 19.11.).
 */
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

    /** Display names by ingredient id, so the search also finds dishes by their ingredients. */
    private var ingredientNames: Map<Long, String> = emptyMap()

    /** Normalized display names of all ingredients, to avoid creating the same name twice. */
    private var knownNames: Set<String> = emptySet()

    /** Declared last: [load] fills the fields above. */
    private val loadJob = viewModelScope.launch { load() }

    private fun defaultTimeOf(slot: MealSlot): ExactTime =
        clock.localTime(clock.timeOf(slot)).let { (hour, minute) -> ExactTime(daysAgo = 0, hour = hour, minute = minute) }

    private suspend fun load() {
        val ingredients = repository.ingredients()
        ingredientsById = ingredients.associateBy { it.id }
        val synonyms = repository.synonyms(language.language())
        search = IngredientSearch(
            ingredients.map { SearchEntry(it.id, displayName(it), synonyms[it.id].orEmpty()) },
        )
        ingredientNames = ingredients.associate { it.id to displayName(it) }
        knownNames = ingredientNames.values.map(::normalizeForSearch).toSet()
        val allergies = repository.ingredientIdsWithKnownAllergy()
        val needsPreparation = repository.ingredientIdsNeedingPreparation()
        val recent = recentIngredients()
        val favorites = repository.favorites()
        _state.update {
            it.copy(loading = false, today = clock.today(), recent = recent, favorites = favorites, needsPreparation = needsPreparation).withAllergies(allergies)
        }
    }

    /** Opens a logged meal from the history: its ingredients, slot and time. */
    fun startEditing(mealId: Long) {
        if (_state.value.editingMealId == mealId) return
        viewModelScope.launch {
            loadJob.join()
            val meal = repository.meal(mealId) ?: return@launch
            val (hour, minute) = clock.localTime(meal.eatenAt)
            val daysAgo = (clock.today() - clock.epochDayOf(meal.eatenAt)).toInt()
            _state.update { state ->
                state.copy(
                    editingMealId = mealId,
                    slot = MealSlot.at(hour, minute) ?: MealSlot.SNACK,
                    slotTime = null,
                    exactTime = ExactTime(daysAgo, hour, minute),
                    day = daysAgo,
                    favoriteId = meal.favoriteId,
                    selected = meal.items.mapNotNull { item ->
                        ingredientsById[item.ingredientId]?.let { SelectedIngredient(it, item.portion, item.preparation) }
                    },
                )
            }
        }
    }

    /** "Log" on a dish in the favourites (self-test 19.11.): the form starts with its ingredients; the meal is chosen as usual. */
    fun startWithFavorite(favoriteId: Long) {
        if (_state.value.favoriteId == favoriteId) return
        viewModelScope.launch {
            loadJob.join()
            _state.value.favorites.firstOrNull { it.id == favoriteId }?.let(::onFavoriteClick)
        }
    }


    /**
     * "+ Essen" on a day in the history (feedback 19.11.): the form starts on that day; choosing
     * breakfast, lunch or dinner keeps the day (onSlotChange).
     */
    fun startForDay(daysAgo: Int) {
        onDayChange(daysAgo)
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
                // Self-test 19.11.: a dish can be found by its name or an ingredient.
                dishResults = if (query.isBlank()) emptyList() else it.favorites.filter { dish -> dish.matches(query, ingredientNames) },
                nameExists = normalizeForSearch(query) in knownNames,
            )
        }
    }

    /** Today: the slot's default time. An earlier day (a picked or edited time) keeps its day. */
    /** The day row (feedback 19.11.): today or up to five days back; the meal's time follows. */
    fun onDayChange(daysAgo: Int) {
        val day = daysAgo.coerceAtLeast(0)
        _state.update { it.copy(day = day) }
        _state.value.slot?.let(::onSlotChange)
    }

    /** Today: the slot's default time. An earlier day: the slot's fixed time on that day (snack keeps its clock time). */
    fun onSlotChange(slot: MealSlot) {
        val day = _state.value.day
        if (day == 0) {
            _state.update { it.copy(slot = slot, slotTime = defaultTimeOf(slot), exactTime = null, slotMissing = false) }
        } else {
            val shown = _state.value.shownTime
            val time = if (slot == MealSlot.SNACK) {
                ExactTime(day, shown?.hour ?: PAST_DAY_HOUR, shown?.minute ?: 0)
            } else {
                ExactTime(day, slot.hour, slot.minute)
            }
            _state.update { it.copy(slot = slot, slotTime = null, exactTime = time, slotMissing = false) }
        }
    }

    /** The time dialog only changes the clock time; the day comes from the day row. */
    fun onExactTimeChange(time: ExactTime) {
        _state.update { it.copy(exactTime = time.copy(daysAgo = it.day)) }
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
                dishResults = emptyList(),
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
            state.copy(selected = state.selected + added, favoriteId = favorite.id, query = "", results = emptyList(), dishResults = emptyList())
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

    fun onSave() = save(checkExisting = true)

    /** Answer to "there is already a dinner today": add the ingredients there. */
    fun onAddToExisting() {
        val existing = _state.value.existingMealId ?: return
        _state.update { it.copy(existingMealId = null, saving = true) }
        viewModelScope.launch {
            val current = _state.value
            val meal = repository.meal(existing)
            if (meal == null) {
                _state.update { it.copy(saving = false) }
                return@launch
            }
            val added = current.mealItems
            // An ingredient that is in both keeps the new portion.
            val merged = meal.items.filter { old -> added.none { it.ingredientId == old.ingredientId } } + added
            repository.updateMeal(meal.id, meal.eatenAt, clock.offsetMinAt(meal.eatenAt), merged, meal.favoriteId)
            if (current.saveAsFavorite) repository.saveFavorite(current.favoriteName.trim(), added)
            resetForm(savedMealId = null, addedTo = current.slot)
        }
    }

    /** Answer to "there is already a dinner today": log it as a meal of its own. */
    fun onSaveSeparately() {
        _state.update { it.copy(existingMealId = null) }
        save(checkExisting = false)
    }

    fun onExistingDismissed() {
        _state.update { it.copy(existingMealId = null) }
    }

    private fun save(checkExisting: Boolean) {
        val current = _state.value
        if (!current.canSave) return
        val slot = current.slot
        if (slot == null) {
            _state.update { it.copy(slotMissing = true) }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val items = current.mealItems
            val eatenAt = current.exactTime?.let { clock.timeOf(it, 0) } ?: clock.timeOf(slot)
            current.editingMealId?.let { mealId ->
                repository.updateMeal(mealId, eatenAt, clock.offsetMinAt(eatenAt), items, current.favoriteId)
                _state.update { it.copy(saving = false, finished = true) }
                return@launch
            }
            // Breakfast, lunch and dinner have a fixed time: the same slot on the same day is the same meal.
            val existing = if (checkExisting && slot != MealSlot.SNACK) repository.mealIdAt(eatenAt) else null
            if (existing != null) {
                _state.update { it.copy(saving = false, existingMealId = existing) }
                return@launch
            }
            val favoriteId = if (current.saveAsFavorite) {
                repository.saveFavorite(current.favoriteName.trim(), items)
            } else {
                current.favoriteId
            }
            val mealId = repository.saveMeal(eatenAt = eatenAt, tzOffsetMin = clock.offsetMinAt(eatenAt), items = items, favoriteId = favoriteId)
            resetForm(savedMealId = mealId)
        }
    }

    /** Back to a fresh form; allergies and catalog stay as loaded. */
    private suspend fun resetForm(savedMealId: Long?, addedTo: MealSlot? = null) {
        val recent = recentIngredients()
        val favorites = repository.favorites()
        _state.update {
            it.copy(
                query = "",
                results = emptyList(),
                dishResults = emptyList(),
                selected = emptyList(),
                slot = null,
                slotTime = null,
                exactTime = null,
                slotMissing = false,
                favoriteId = null,
                saveAsFavorite = false,
                favoriteName = "",
                saving = false,
                savedMealId = savedMealId,
                addedTo = addedTo,
                recent = recent,
                favorites = favorites,
            )
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
        _state.update { it.copy(savedMealId = null, addedTo = null) }
    }

    companion object {
        private const val RECENT_LIMIT = 12

        /** Time of a meal added for an earlier day until a meal is chosen. */
        private const val PAST_DAY_HOUR = 12
    }
}
