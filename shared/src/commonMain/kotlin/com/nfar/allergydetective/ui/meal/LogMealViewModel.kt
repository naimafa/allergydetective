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
import com.nfar.allergydetective.domain.diary.PackageIngredients
import com.nfar.allergydetective.domain.diary.PackageWordMatcher
import com.nfar.allergydetective.domain.diary.LoggedMeal
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
        val earlier = earlierMeals()
        val favorites = repository.favorites()
        _state.update {
            it.copy(
                loading = false, today = clock.today(), recent = recent, earlierMeals = earlier, favorites = favorites, needsPreparation = needsPreparation,
                minorIngredientIds = ingredients.filter { ingredient -> ingredient.categoryId in MINOR_CATEGORY_IDS }.map { ingredient -> ingredient.id }.toSet(),
            ).withAllergies(allergies)
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
                    // User 24.11.2026: the meal opens as it was logged - dish cards and single ingredients.
                    selected = singlesOf(meal),
                    dishes = dishesOf(meal),
                )
            }
        }
    }

    /** The dishes of a logged meal from its snapshot; a dish deleted since gets a negative key. */
    private fun dishesOf(meal: LoggedMeal): List<ChosenDish> =
        meal.snapshot?.dishes.orEmpty().mapIndexed { index, dish ->
            ChosenDish(
                favoriteId = dish.favoriteId ?: -(meal.id * 100 + index + 1),
                portion = dish.portion,
                name = dish.name,
                items = dish.items,
                originFavoriteId = dish.favoriteId,
            )
        }

    /** The single ingredients of a logged meal; a meal from before the snapshot opens with all its ingredients. */
    private fun singlesOf(meal: LoggedMeal): List<SelectedIngredient> =
        (meal.snapshot?.singles ?: meal.items).mapNotNull { item ->
            ingredientsById[item.ingredientId]?.let { SelectedIngredient(it, item.portion, item.preparation) }
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

    /** Yesterday's and the day before yesterday's meals, newest day first, in the order of the day. */
    private suspend fun earlierMeals(): List<EarlierMeal> {
        val today = clock.today()
        return (1..EARLIER_DAYS).flatMap { daysAgo ->
            val from = clock.startOfDayMillis(today - daysAgo)
            val until = clock.startOfDayMillis(today - daysAgo + 1)
            MealSlot.entries.mapNotNull { slot -> repository.mealIdOnDay(from, until, slot)?.let { EarlierMeal(it, daysAgo, slot) } }
        }
    }

    /** Takes a logged meal over into the form: its dishes and single ingredients are added, nothing is replaced. */
    fun onCopyEarlierMeal(earlier: EarlierMeal) {
        viewModelScope.launch {
            val meal = repository.meal(earlier.mealId) ?: return@launch
            val singles = singlesOf(meal)
            val dishes = dishesOf(meal)
            _state.update { state ->
                val addedDishes = dishes.filter { dish -> state.dishes.none { it.favoriteId == dish.favoriteId } }
                state.copy(
                    selected = state.selected + singles.filter { single -> state.selected.none { it.ingredient.id == single.ingredient.id } },
                    dishes = state.dishes + addedDishes,
                    favoriteId = state.favoriteId ?: addedDishes.firstNotNullOfOrNull { it.originFavoriteId },
                    query = "",
                    results = emptyList(),
                    dishResults = emptyList(),
                )
            }
            suggestDrink()
        }
    }

    private suspend fun recentIngredients(): List<IngredientInfo> =
        repository.recentIngredientIds(RECENT_LIMIT).mapNotNull { ingredientsById[it] }
            // Self-test 18.11.: a custom and a seed ingredient can share a name; show it once.
            .distinctBy { normalizeForSearch(displayName(it)) }

    /** An ingredient by id, e.g. for the lines inside a chosen dish. */
    fun ingredientInfo(id: Long): IngredientInfo? = ingredientsById[id]

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
                // User 25.11.2026: typing a dish's name offered it as a new ingredient, and dishes ended up in
                // "Meine Zutaten". A dish name counts as taken, too.
                nameExists = normalizeForSearch(query).let { typed ->
                    typed in knownNames || it.favorites.any { dish -> normalizeForSearch(dish.name) == typed }
                },
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
                openedDishIds = emptySet(),
                dishes = state.dishes.filterNot { it.favoriteId in state.openedDishIds },
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
            val mealId = existingMealOnDay(eatenAt, slot) ?: return@launch
            val meal = repository.meal(mealId) ?: return@launch
            val (hour, minute) = clock.localTime(meal.eatenAt)
            _state.update { state ->
                if (state.slot != slot || state.editingMealId != null) return@update state
                val chosen = state.selected
                val old = singlesOf(meal).filter { item -> chosen.none { it.ingredient.id == item.ingredient.id } }
                val oldDishes = dishesOf(meal).filter { dish -> state.dishes.none { it.favoriteId == dish.favoriteId } }
                state.copy(
                    editingMealId = mealId,
                    openedExisting = slot,
                    openedItemIds = old.map { it.ingredient.id }.toSet(),
                    openedDishIds = oldDishes.map { it.favoriteId }.toSet(),
                    slotTime = null,
                    exactTime = ExactTime(state.day, hour, minute),
                    favoriteId = state.favoriteId ?: meal.favoriteId,
                    selected = old + chosen,
                    dishes = oldDishes + state.dishes,
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
    /** The clock time now on the chosen day: the start of the time field before a meal is chosen. */
    fun nowTime(): ExactTime = clock.localTime(clock.nowMillis()).let { (h, m) -> ExactTime(_state.value.day, h, m) }

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
        val items = state.mealItems
        val onlyDrinks = items.isNotEmpty() && items.all { ingredientsById[it.ingredientId]?.categoryId == BEVERAGES_CATEGORY_ID }
        if (state.slot == null && onlyDrinks) {
            onSlotChange(MealSlot.DRINK)
            _state.update { it.copy(slotSuggested = true) }
        } else if (state.slotSuggested && !onlyDrinks) {
            _state.update { it.copy(slot = null, slotTime = null, exactTime = null, slotSuggested = false) }
        }
    }

    /**
     * A tap on an ingredient (search result or quick pick). Already picked on its own: it is taken out (asking when it
     * sits in several places). Already inside a chosen dish: asks first whether to add it as well (user 24.11.2026).
     */
    fun onToggleIngredient(ingredient: IngredientInfo) {
        val state = _state.value
        val places = state.placesOf(ingredient.id)
        when {
            Place.Single in places -> requestRemove(ingredient, Place.Single)
            places.isNotEmpty() -> _state.update { it.copy(pendingAdd = PendingAdd(ingredient, places.filterIsInstance<Place.InDish>().map { place -> place.favoriteId })) }
            else -> addSingle(ingredient)
        }
    }

    private fun addSingle(ingredient: IngredientInfo) {
        _state.update { state ->
            state.copy(
                selected = state.selected + SelectedIngredient(ingredient),
                query = "",
                results = emptyList(),
                dishResults = emptyList(),
                pendingAdd = null,
            )
        }
        suggestDrink()
    }

    /** "Yes, add it as well": the ingredient is also picked on its own, next to the dish that has it. */
    fun onConfirmAddAnyway() {
        val pending = _state.value.pendingAdd ?: return
        addSingle(pending.ingredient)
    }

    /** "No, use the one already there": nothing is added. */
    fun onDeclineAdd() {
        _state.update { it.copy(pendingAdd = null, query = "", results = emptyList(), dishResults = emptyList()) }
    }

    /** The bin on an ingredient row: [dishId] is the dish it sits in, null for one picked on its own. */
    fun onRemoveIngredient(ingredientId: Long, dishId: Long?) {
        val ingredient = ingredientsById[ingredientId] ?: return
        requestRemove(ingredient, if (dishId == null) Place.Single else Place.InDish(dishId))
    }

    /** One place: removed at once. Several: the screen asks whether to remove it everywhere or only here. */
    private fun requestRemove(ingredient: IngredientInfo, origin: Place) {
        val places = _state.value.placesOf(ingredient.id)
        // User 24.11.2026: always ask, a tap on the bin can be a mistake.
        if (places.isNotEmpty()) {
            _state.update { it.copy(pendingRemove = PendingRemove(ingredient, places, origin)) }
        } else {
            removeFrom(ingredient.id, origin)
        }
    }

    fun onRemoveEverywhere() {
        val pending = _state.value.pendingRemove ?: return
        pending.places.forEach { removeFrom(pending.ingredient.id, it) }
        _state.update { it.copy(pendingRemove = null) }
    }

    fun onRemoveHereOnly() {
        val pending = _state.value.pendingRemove ?: return
        removeFrom(pending.ingredient.id, pending.origin)
        _state.update { it.copy(pendingRemove = null) }
    }

    fun onRemoveCancelled() {
        _state.update { it.copy(pendingRemove = null) }
    }

    private fun removeFrom(ingredientId: Long, place: Place) {
        _state.update { state ->
            val next = when (place) {
                Place.Single -> state.copy(selected = state.selected.filterNot { it.ingredient.id == ingredientId })
                is Place.InDish -> state.copy(
                    dishes = state.dishes.map { if (it.favoriteId == place.favoriteId) it.copy(removed = it.removed + ingredientId) else it },
                )
            }
            // A dish without any ingredient left is gone.
            val dishes = next.dishes.filter { next.dishItems(it).isNotEmpty() }
            next.copy(dishes = dishes, favoriteId = if (dishes.isNotEmpty()) next.favoriteId.takeIf { id -> dishes.any { it.favoriteId == id } } ?: dishes.last().favoriteId else next.favoriteId.takeIf { next.selected.isNotEmpty() })
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
        // What the meal already has (also inside dishes) is not asked about again by voice.
        val present = _state.value.mealItems.map { it.ingredientId }.toSet()
        match.ingredientIds
            .filter { it !in present }
            .mapNotNull(ingredientsById::get)
            .forEach(::onToggleIngredient)
        if (match.unmatched.isNotEmpty()) onQueryChange(match.unmatched.joinToString(" "))
    }

    /**
     * Text read from a package photo (user 27.11.2026): only names as written count, no guessing. What the catalog
     * does not know is offered as a new ingredient; the user confirms everything.
     */
    fun onPackageText(text: String?) {
        if (text == null) return
        val current = _state.value
        val match = PackageWordMatcher(ingredientNames).match(PackageIngredients.clean(text))
        val present = current.mealItems.map { it.ingredientId }.toSet()
        val found = match.ingredientIds.filter { it !in present }.mapNotNull(ingredientsById::get)
        _state.update { it.copy(packageScan = PackageScan(found, match.unknown.take(MAX_UNRECOGNIZED))) }
    }

    /** Adds the ticked ingredients; the ticked unknown names are asked one after the other in the "new ingredient" form. */
    fun onPackageConfirm(ingredientIds: Set<Long>, newNames: List<String> = emptyList()) {
        val scan = _state.value.packageScan ?: return
        _state.update { it.copy(packageScan = null, newFromPackage = newNames) }
        scan.found.filter { it.id in ingredientIds }.forEach { ingredient ->
            if (_state.value.mealItems.none { it.ingredientId == ingredient.id }) onToggleIngredient(ingredient)
        }
    }

    /** The "new ingredient" form for the first waiting name is done (created or skipped): the next one follows. */
    fun onNewFromPackageHandled() {
        _state.update { it.copy(newFromPackage = it.newFromPackage.drop(1)) }
    }

    fun onPackageDismiss() {
        _state.update { it.copy(packageScan = null) }
    }

    /**
     * Adds a dish with one portion for the whole dish (user 24.11.2026); a tap on a dish that is already chosen takes
     * it out again. The portion starts at the largest one the saved dish had for its main part.
     */
    fun onFavoriteClick(favorite: FavoriteInfo) {
        if (_state.value.dishes.any { it.favoriteId == favorite.id }) return onRemoveDish(favorite.id)
        _state.update { state ->
            val main = favorite.items.filter { it.ingredientId !in state.minorIngredientIds }.ifEmpty { favorite.items }
            val start = main.maxOfOrNull { it.portion } ?: Portion.MEDIUM
            state.copy(
                dishes = state.dishes + ChosenDish(favorite.id, start, name = favorite.name, items = favorite.items),
                favoriteId = favorite.id,
                query = "",
                results = emptyList(),
                dishResults = emptyList(),
            )
        }
        suggestDrink()
    }

    /** Takes a dish out with everything that belongs to it; single ingredients stay. */
    fun onRemoveDish(dishId: Long) {
        _state.update { state ->
            val dishes = state.dishes.filterNot { it.favoriteId == dishId }
            val favoriteId = state.favoriteId.takeIf { it != dishId } ?: dishes.lastOrNull()?.favoriteId
            state.copy(dishes = dishes, favoriteId = favoriteId)
        }
        suggestDrink()
    }

    /** One portion for the whole dish. */
    fun onDishPortionChange(dishId: Long, portion: Portion) {
        _state.update { state -> state.copy(dishes = state.dishes.map { if (it.favoriteId == dishId) it.copy(portion = portion) else it }) }
    }

    fun onDishPreparationChange(dishId: Long, ingredientId: Long, preparation: Preparation) {
        _state.update { state ->
            state.copy(dishes = state.dishes.map { if (it.favoriteId == dishId) it.copy(preparations = it.preparations + (ingredientId to preparation)) else it })
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

    /** "normal / glutenfrei" for a product that has a gluten-free version (user 25.11.2026). */
    fun onGlutenFreeChange(ingredientId: Long, glutenFree: Boolean) {
        _state.update { state ->
            state.copy(
                selected = state.selected.map {
                    if (it.ingredient.id == ingredientId) it.copy(preparation = Preparation.GLUTEN_FREE.takeIf { glutenFree }) else it
                },
            )
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
            // The snapshot of the existing meal plus what was chosen now (user 24.11.2026).
            val before = meal.snapshot ?: com.nfar.allergydetective.domain.diary.MealSnapshot(emptyList(), meal.items)
            val now = current.snapshot
            val snapshot = com.nfar.allergydetective.domain.diary.MealSnapshot(before.dishes + now.dishes, before.singles + now.singles)
            repository.updateMeal(meal.id, meal.eatenAt, clock.offsetMinAt(meal.eatenAt), merged, meal.favoriteId, meal.slot ?: current.slot, snapshot)
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

    /** The meal of [slot] on the local day of [eatenAt]: a changed time must not start a second breakfast (user 24.11.2026). */
    private suspend fun existingMealOnDay(eatenAt: Long, slot: MealSlot): Long? {
        val day = clock.epochDayOf(eatenAt)
        return repository.mealIdOnDay(clock.startOfDayMillis(day), clock.startOfDayMillis(day + 1), slot)
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
                repository.updateMeal(mealId, eatenAt, clock.offsetMinAt(eatenAt), items, current.favoriteId, slot, current.snapshot)
                _state.update { it.copy(saving = false, finished = true) }
                return@launch
            }
            // Breakfast, lunch and dinner have a fixed time: the same slot on the same day is the same meal.
            val existing = if (checkExisting && slot.hasFixedTime) existingMealOnDay(eatenAt, slot) else null
            if (existing != null) {
                _state.update { it.copy(saving = false, existingMealId = existing) }
                return@launch
            }
            val favoriteId = if (current.saveAsFavorite) {
                repository.saveFavorite(current.favoriteName.trim(), items)
            } else {
                current.favoriteId
            }
            val mealId = repository.saveMeal(eatenAt = eatenAt, tzOffsetMin = clock.offsetMinAt(eatenAt), items = items, favoriteId = favoriteId, slot = slot, snapshot = current.snapshot)
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
                dishes = emptyList(),
                pendingAdd = null,
                pendingRemove = null,
                slot = null,
                slotTime = null,
                exactTime = null,
                slotMissing = false,
                slotSuggested = false,
                favoriteId = null,
                saveAsFavorite = false,
                favoriteName = "",
                saving = false,
                // User 25.11.2026: after saving the dashboard opens; the history serves for correcting.
                finished = true,
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
        private const val MAX_UNRECOGNIZED = 12
        private const val EARLIER_DAYS = 2

        /** Seed categories nuts and seeds, oils and fats, spices and herbs, herbs (categories.json). */
        internal val MINOR_CATEGORY_IDS = setOf<Long?>(9L, 13L, 14L, 15L)

        /** Time of a meal added for an earlier day until a meal is chosen. */
        private const val PAST_DAY_HOUR = 12
    }
}
