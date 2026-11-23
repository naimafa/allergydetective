package com.nfar.allergydetective.ui.meal

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.ui.common.ExactTime

data class SelectedIngredient(
    val ingredient: IngredientInfo,
    val portion: Portion = Portion.MEDIUM,
    /** Raw or cooked; must be chosen for ingredients in [LogMealState.needsPreparation]. */
    val preparation: Preparation? = null,
    /**
     * The dishes it came with (user 23.11.2026: one list for the whole meal, an ingredient of two dishes once,
     * with "aus: Gemüsesuppe · Gemüsepfanne" below). Only for the screen; the meal stores one favourite as before.
     */
    val dishIds: Set<Long> = emptySet(),
    /** Picked on its own (search, quick pick, voice): it stays when its dishes are taken out. */
    val alone: Boolean = true,
)

data class LogMealState(
    val loading: Boolean = true,
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    /** Dishes matching the search by name or ingredient. */
    val dishResults: List<FavoriteInfo> = emptyList(),
    val recent: List<IngredientInfo> = emptyList(),
    val favorites: List<FavoriteInfo> = emptyList(),
    val selected: List<SelectedIngredient> = emptyList(),
    /**
     * Breakfast, lunch, dinner or a snack. Nothing is preselected (self-test 19.11.: the time-based
     * preselection led to wrong entries); saving asks for it first.
     */
    val slot: MealSlot? = null,
    /** Default time of [slot]: its fixed time (never in the future), "now" for a snack. Shown below the chips. */
    val slotTime: ExactTime? = null,
    /** Day of the meal from the day row: 0 = today, up to MAX_DAYS_BACK (feedback 19.11.). */
    val day: Int = 0,
    /** Local epoch day of today, for the date in the day row. */
    val today: Long = 0,
    /** Time changed in the dialog ("Change"), replaces [slotTime]. */
    val exactTime: ExactTime? = null,
    /** Save was tapped without a [slot]: the "when" step is highlighted. */
    val slotMissing: Boolean = false,
    /** [slot] is the automatic "drink" for a drinks-only selection, not yet confirmed by the user (feedback 20.11.). */
    val slotSuggested: Boolean = false,
    /** Favorite the selection came from, stored as origin of the meal (TRD 3.2). */
    val favoriteId: Long? = null,
    val saveAsFavorite: Boolean = false,
    val favoriteName: String = "",
    val saving: Boolean = false,
    /** The search text is exactly the name of an existing ingredient: no "create new" then. */
    val nameExists: Boolean = false,
    /** Set after saving, so the screen can offer undo. Cleared with [LogMealViewModel.onSavedShown]. */
    val savedMealId: Long? = null,
    /** Editing a logged meal from the history (self-test 19.11.): saving replaces it. */
    val editingMealId: Long? = null,
    /** The chosen slot was already logged that day and is now open for editing (a hint says so). */
    val openedExisting: MealSlot? = null,
    /** Ingredients that came from that opened meal; they go again when another slot is chosen. */
    val openedItemIds: Set<Long> = emptySet(),
    /** There is already a meal at this slot, e.g. today's dinner: the screen asks whether to add to it. */
    val existingMealId: Long? = null,
    /** The ingredients were added to the existing meal of this slot: a short confirmation without undo. */
    val addedTo: MealSlot? = null,
    /** The edited meal is saved: the screen closes. */
    val finished: Boolean = false,
    private val allergyIngredientIds: Set<Long> = emptySet(),
    /** Ingredients with a heat-labile trigger (birch pollen): raw or cooked is mandatory. */
    val needsPreparation: Set<Long> = emptySet(),
) {
    val canSave: Boolean get() =
        selected.isNotEmpty() && !saving && !missingPreparation && (!saveAsFavorite || favoriteName.isNotBlank())

    /** The time that will be stored: the changed one or the slot's default. */
    val shownTime: ExactTime? get() = exactTime ?: slotTime

    val missingPreparation: Boolean get() = selected.any { it.ingredient.id in needsPreparation && it.preparation == null }

    /** UX 3.1: a gentle hint, saving stays possible. */
    val containsKnownAllergy: Boolean get() = selected.any { it.ingredient.id in allergyIngredientIds }

    /** What gets stored; raw / cooked only where it was asked. */
    val mealItems: List<MealItem> get() =
        selected.map { MealItem(it.ingredient.id, it.portion, if (it.ingredient.id in needsPreparation) it.preparation else null) }

    internal fun withAllergies(ids: Set<Long>) = copy(allergyIngredientIds = ids)
}
