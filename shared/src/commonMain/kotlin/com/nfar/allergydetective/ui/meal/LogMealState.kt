package com.nfar.allergydetective.ui.meal

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.GLUTEN_FREE_OPTION_IDS
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.MealSnapshot
import com.nfar.allergydetective.domain.diary.SnapshotDish
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.ui.common.ExactTime

/** An ingredient picked on its own, with its own portion. The ingredients of dishes are in [ChosenDish]. */
data class SelectedIngredient(
    val ingredient: IngredientInfo,
    val portion: Portion = Portion.MEDIUM,
    /** Raw or cooked; must be chosen for ingredients in [LogMealState.needsPreparation]. */
    val preparation: Preparation? = null,
)

/**
 * A dish chosen for the meal (user 24.11.2026): it has one portion for the whole dish. Its ingredients come from the
 * saved dish; [removed] are the ones left out of this meal only.
 */
data class ChosenDish(
    /** Key of the dish in this form: the dish id, or a negative id for a logged dish that was deleted since. */
    val favoriteId: Long,
    val portion: Portion = Portion.MEDIUM,
    /** Name as logged (snapshot, user 24.11.2026). */
    val name: String = "",
    /** Its ingredients as logged or as the dish had them when it was picked; a later change of the dish does not reach them. */
    val items: List<MealItem> = emptyList(),
    /** The saved dish it came from, null once that dish is deleted. */
    val originFavoriteId: Long? = favoriteId.takeIf { it > 0 },
    val removed: Set<Long> = emptySet(),
    /** Raw / cooked for ingredients that need it and have no value in the saved dish. */
    val preparations: Map<Long, Preparation> = emptyMap(),
)

/** Where an ingredient sits in the meal: picked on its own, or inside a chosen dish. */
sealed interface Place {
    data object Single : Place
    data class InDish(val favoriteId: Long) : Place
}

/** A logged meal of an earlier day that can be copied into the form. */
data class EarlierMeal(val mealId: Long, val daysAgo: Int, val slot: MealSlot)

/** "Tomate steckt schon in …": the ingredient is already in the chosen dishes; the user decides whether to add it too. */
data class PendingAdd(val ingredient: IngredientInfo, val dishIds: List<Long>)

/** "Tomate kommt an 3 Stellen vor": all places, and the one the tap came from. */
data class PendingRemove(val ingredient: IngredientInfo, val places: List<Place>, val origin: Place)

data class LogMealState(
    val loading: Boolean = true,
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    /** Dishes matching the search by name or ingredient. */
    val dishResults: List<FavoriteInfo> = emptyList(),
    val recent: List<IngredientInfo> = emptyList(),
    /** Meals of yesterday and the day before, to take over again ("Wie gestern?", user 27.11.2026). */
    val earlierMeals: List<EarlierMeal> = emptyList(),
    val favorites: List<FavoriteInfo> = emptyList(),
    /** Ingredients picked on their own; the ingredients of dishes are in [dishes]. */
    val selected: List<SelectedIngredient> = emptyList(),
    /** Chosen dishes in the order they were picked, each with one portion. */
    val dishes: List<ChosenDish> = emptyList(),
    val pendingAdd: PendingAdd? = null,
    val pendingRemove: PendingRemove? = null,
    /** Nuts and seeds, oils, spices and herbs: counted one step smaller inside a dish. */
    val minorIngredientIds: Set<Long> = emptySet(),
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
    /** Dishes that came from that opened meal; they go again when another slot is chosen. */
    val openedDishIds: Set<Long> = emptySet(),
    /** There is already a meal at this slot, e.g. today's dinner: the screen asks whether to add to it. */
    val existingMealId: Long? = null,
    /** The ingredients were added to the existing meal of this slot: a short confirmation without undo. */
    val addedTo: MealSlot? = null,
    /** The edited meal is saved: the screen closes. */
    val finished: Boolean = false,
    private val allergyIngredientIds: Set<Long> = emptySet(),
    /** Ingredients with a heat-labile trigger (birch pollen): raw or cooked is mandatory. */
    val needsPreparation: Set<Long> = emptySet(),
    /** Products with a gluten-free version: "normal / glutenfrei" is offered, normal by default (user 25.11.2026). */
    val glutenFreeOption: Set<Long> = GLUTEN_FREE_OPTION_IDS,
) {
    val canSave: Boolean get() =
        mealItems.isNotEmpty() && !saving && !missingPreparation && (!saveAsFavorite || favoriteName.isNotBlank())

    /** The time that will be stored: the changed one or the slot's default. */
    val shownTime: ExactTime? get() = exactTime ?: slotTime

    val missingPreparation: Boolean get() = mealItems.any { it.ingredientId in needsPreparation && it.preparation == null }

    /** UX 3.1: a gentle hint, saving stays possible. */
    val containsKnownAllergy: Boolean get() = mealItems.any { it.ingredientId in allergyIngredientIds }

    /** Raw / cooked only where it was asked; gluten-free only for products that have that version. */
    private fun kept(ingredientId: Long, preparation: Preparation?): Preparation? = when {
        preparation == null -> null
        ingredientId in needsPreparation -> preparation.takeIf { it != Preparation.GLUTEN_FREE }
        preparation == Preparation.GLUTEN_FREE && ingredientId in glutenFreeOption -> preparation
        else -> null
    }

    /** The ingredients of a chosen dish that stay in this meal. */
    fun dishItems(dish: ChosenDish): List<MealItem> =
        dish.items.filter { it.ingredientId !in dish.removed }

    /**
     * What gets stored: the ingredients of the dishes, each with its dish's portion (user 24.11.2026), and the single
     * ingredients. An ingredient that sits in several places counts once, with the largest portion. Raw / cooked only
     * where it was asked.
     */
    val mealItems: List<MealItem> get() {
        val fromDishes = dishes.flatMap(::countedItems)
        val alone = selected.map { MealItem(it.ingredient.id, it.portion, it.preparation) }
        return (fromDishes + alone).groupBy { it.ingredientId }.map { (id, same) ->
            MealItem(
                ingredientId = id,
                portion = same.maxOf { it.portion },
                preparation = kept(id, same.firstNotNullOfOrNull { it.preparation }),
            )
        }
    }

    /** The ingredients of a chosen dish with the portion that counts (seeds, spices, oils and herbs one step smaller). */
    fun countedItems(dish: ChosenDish): List<MealItem> {
        val items = dishItems(dish)
        // A dish made only of small parts is not shrunk.
        val hasMainPart = items.any { it.ingredientId !in minorIngredientIds }
        return items.map { item ->
            val portion = if (hasMainPart && item.ingredientId in minorIngredientIds) dish.portion.smaller() else dish.portion
            val preparation = kept(item.ingredientId, dish.preparations[item.ingredientId] ?: item.preparation)
            MealItem(item.ingredientId, portion, preparation)
        }
    }

    /** What gets stored besides the merged list: the dishes as logged and the single ingredients (user 24.11.2026). */
    val snapshot: MealSnapshot get() = MealSnapshot(
        dishes = dishes.map { SnapshotDish(it.originFavoriteId, it.name, it.portion, countedItems(it)) },
        singles = selected.map { MealItem(it.ingredient.id, it.portion, kept(it.ingredient.id, it.preparation)) },
    )

    /** Where the ingredient sits in this meal: on its own and/or in which chosen dishes. */
    fun placesOf(ingredientId: Long): List<Place> = buildList {
        if (selected.any { it.ingredient.id == ingredientId }) add(Place.Single)
        dishes.forEach { dish -> if (dishItems(dish).any { it.ingredientId == ingredientId }) add(Place.InDish(dish.favoriteId)) }
    }

    internal fun withAllergies(ids: Set<Long>) = copy(allergyIngredientIds = ids)
}

/** One step smaller, small stays small. */
fun Portion.smaller(): Portion = when (this) {
    Portion.LARGE -> Portion.MEDIUM
    else -> Portion.SMALL
}
