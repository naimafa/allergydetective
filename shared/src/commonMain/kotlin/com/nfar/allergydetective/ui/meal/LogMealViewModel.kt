package com.nfar.allergydetective.ui.meal

import com.nfar.allergydetective.domain.diary.matchingIngredient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.BEVERAGES_CATEGORY_ID
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
                    slot = meal.slot ?: MealSlot.at(hour, minute) ?: MealSlot.SNACK,
                    slotTime = null,
                    exactTime = ExactTime(daysAgo, hour, minute),
                    day = daysAgo,
                    favoriteId = meal.favoriteId,
                    selected = meal.items.mapNotNull { item ->
                        ingredientsById[item.ingredientId]?.let { SelectedIngredient(it, item.portion, item.preparation, dishIds = setOfNotNull(dishOf(state, meal.favoriteId, item.ingredientId)), alone = dishOf(state, meal.favoriteId, item.ingredientId) == null) }
                    },
                )
            }
        }
    }

    /** The dish an ingredient of an edited meal belongs to: the meal's favourite, if the ingredient is part of it. */
    private fun dishOf(state: LogMealState, favoriteId: Long?, ingredientId: Long): Long? =
        favoriteId?.takeIf { id -> state.favorites.firstOrNull { it.id == id }?.items?.any { it.ingredientId == ingredientId } == true }

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

    /** Other name through which an ingredient was found, shown as "also: …" (e.g. Quark – auch: Topfen). */
    fun synonymReason(ingredientId: Long, query: String): String? = search.matchedSynonym(ingredientId, query)

    /** Ingredient through which a saved dish was found, shown as "contains …". */
    fun dishReason(dish: com.nfar.allergydetective.domain.diary.FavoriteInfo, query: String): String? = dish.matchingIngredient(query, ingredientNames)

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
        val opened = _state.value.openedExisting != null
        leaveOpenedMeal()
        _state.update { it.copy(day = day) }
        _state.value.slot?.let(::onSlotChange)
        // The other day may have its own lunch: open that one instead.
        if (opened || _state.value.editingMealId == null) _state.value.slot?.let(::openExistingMeal)
    }

    /** A tap on a meal chip; a tap on the chosen one takes the choice back (user 23.11.2026: it could not be undone). */
    fun onSlotTap(slot: MealSlot) {
        leaveOpenedMeal()
        if (_state.value.slot == slot) {
            _state.update { it.copy(slot = null, slotTime = null, exactTime = null, slotSuggested = false) }
            return
        }
        onSlotChange(slot)
        openExistingMeal(slot)
    }

    /** Leaving a meal that was opened only because its slot was chosen: back to a new meal, without its ingredients. */
    private fun leaveOpenedMeal() {
        if (_state.value.openedExisting == null) return
        _state.update { state ->
            state.copy(
                editingMealId = null,
                openedExisting = null,
                openedItemIds = emptySet(),
                selected = state.selected.filterNot { it.ingredient.id in state.openedItemIds },
            )
        }
    }

    /**
     * User 23.11.2026: choosing "Mittagessen" when today's lunch is already logged opens that lunch for editing
     * instead of starting a second one. What was chosen so far is added to it (a shared ingredient keeps the new
     * portion). Snack and drink have no fixed time and can be logged several times a day.
     */
    private fun openExistingMeal(slot: MealSlot) {
        val current = _state.value
        if (current.editingMealId != null || !slot.hasFixedTime) return
        viewModelScope.launch {
            val eatenAt = current.exactTime?.let { clock.timeOf(it, 0) } ?: clock.timeOf(slot)
            val mealId = repository.mealIdAt(eatenAt, slot) ?: return@launch
            val meal = repository.meal(mealId) ?: return@launch
            val (hour, minute) = clock.localTime(meal.eatenAt)
            _state.update { state ->
                if (state.slot != slot || state.editingMealId != null) return@update state
                val chosen = state.selected
                val old = meal.items
                    .filter { item -> chosen.none { it.ingredient.id == item.ingredientId } }
                    .mapNotNull { item ->
                        val dish = dishOf(state, meal.favoriteId, item.ingredientId)
                        ingredientsById[item.ingredientId]?.let { SelectedIngredient(it, item.portion, item.preparation, dishIds = setOfNotNull(dish), alone = dish == null) }
                    }
                state.copy(
                    editingMealId = mealId,
                    openedExisting = slot,
                    openedItemIds = old.map { it.ingredient.id }.toSet(),
                    slotTime = null,
                    exactTime = ExactTime(state.day, hour, minute),
                    favoriteId = state.favoriteId ?: meal.favoriteId,
                    selected = old + chosen,
                )
            }
        }
    }

    /**
     * Today: the slot's default time. An earlier day: the slot's fixed time on that day (snack and drink keep their clock time).
     * The chosen slot is stored with the meal, so a later time change keeps it (feedback 20.11.).
     */
    fun onSlotChange(slot: MealSlot) {
        val day = _state.value.day
        if (day == 0) {
            _state.update { it.copy(slot = slot, slotTime = defaultTimeOf(slot), exactTime = null, slotMissing = false, slotSuggested = false) }
        } else {
            val shown = _state.value.shownTime
            val time = if (!slot.hasFixedTime) {
                ExactTime(day, shown?.hour ?: PAST_DAY_HOUR, shown?.minute ?: 0)
            } else {
                ExactTime(day, slot.hour, slot.minute)
            }
            _state.update { it.copy(slot = slot, slotTime = null, exactTime = time, slotMissing = false, slotSuggested = false) }
        }
    }

    /** The time dialog only changes the clock time; the day comes from the day row. */
    fun onExactTimeChange(time: ExactTime) {
        _state.update { it.copy(exactTime = time.copy(daysAgo = it.day), slotSuggested = false) }
    }

    /**
     * Feedback 20.11.: when only drinks are selected and nothing is chosen yet, "drink" is preselected.
     * Adding food takes the suggestion back, unless the user has confirmed it (slot or time tapped).
     */
    private fun suggestDrink() {
        val state = _state.value
        if (state.editingMealId != null) return
        val onlyDrinks = state.selected.isNotEmpty() && state.selected.all { it.ingredient.categoryId == BEVERAGES_CATEGORY_ID }
        if (state.slot == null && onlyDrinks) {
            onSlotChange(MealSlot.DRINK)
            _state.update { it.copy(slotSuggested = true) }
        } else if (state.slotSuggested && !onlyDrinks) {
            _state.update { it.copy(slot = null, slotTime = null, exactTime = null, slotSuggested = false) }
        }
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
        suggestDrink()
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

    /**
     * Adds the ingredients of a favorite with their default portions (UX 3.1); a tap on a dish that is already
     * chosen takes it out again. User 23.11.2026: an ingredient the meal already has is not added twice: it notes
     * the dish and keeps the larger portion.
     */
    fun onFavoriteClick(favorite: FavoriteInfo) {
        if (_state.value.selected.any { favorite.id in it.dishIds }) return onRemoveDish(favorite.id)
        _state.update { state ->
            val portions = favorite.items.associate { it.ingredientId to it.portion }
            val merged = state.selected.map { chosen ->
                portions[chosen.ingredient.id]?.let { portion ->
                    chosen.copy(dishIds = chosen.dishIds + favorite.id, portion = maxOf(chosen.portion, portion))
                } ?: chosen
            }
            val known = state.selected.map { it.ingredient.id }.toSet()
            val added = favorite.items
                .filter { it.ingredientId !in known }
                .mapNotNull { item ->
                    ingredientsById[item.ingredientId]?.let { SelectedIngredient(it, item.portion, item.preparation, dishIds = setOf(favorite.id), alone = false) }
                }
            state.copy(selected = merged + added, favoriteId = favorite.id, query = "", results = emptyList(), dishResults = emptyList())
        }
        suggestDrink()
    }

    /** Takes a dish out: its ingredients go, unless another dish has them or they were picked on their own. */
    fun onRemoveDish(dishId: Long) {
        _state.update { state ->
            val selected = state.selected.mapNotNull { chosen ->
                if (dishId !in chosen.dishIds) return@mapNotNull chosen
                val rest = chosen.dishIds - dishId
                if (rest.isEmpty() && !chosen.alone) null else chosen.copy(dishIds = rest)
            }
            val favoriteId = state.favoriteId.takeIf { it != dishId } ?: selected.flatMap { it.dishIds }.lastOrNull()
            state.copy(selected = selected, favoriteId = favoriteId)
        }
        suggestDrink()
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
            repository.updateMeal(meal.id, meal.eatenAt, clock.offsetMinAt(meal.eatenAt), merged, meal.favoriteId, meal.slot ?: current.slot)
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
                repository.updateMeal(mealId, eatenAt, clock.offsetMinAt(eatenAt), items, current.favoriteId, slot)
                _state.update { it.copy(saving = false, finished = true) }
                return@launch
            }
            // Breakfast, lunch and dinner have a fixed time: the same slot on the same day is the same meal.
            val existing = if (checkExisting && slot.hasFixedTime) repository.mealIdAt(eatenAt, slot) else null
            if (existing != null) {
                _state.update { it.copy(saving = false, existingMealId = existing) }
                return@launch
            }
            val favoriteId = if (current.saveAsFavorite) {
                repository.saveFavorite(current.favoriteName.trim(), items)
            } else {
                current.favoriteId
            }
            val mealId = repository.saveMeal(eatenAt = eatenAt, tzOffsetMin = clock.offsetMinAt(eatenAt), items = items, favoriteId = favoriteId, slot = slot)
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
                slotSuggested = false,
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
