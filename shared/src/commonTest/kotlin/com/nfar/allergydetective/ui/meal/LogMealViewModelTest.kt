package com.nfar.allergydetective.ui.meal

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.BEVERAGES_CATEGORY_ID
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.LoggedMeal
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.ui.common.ExactTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class LogMealViewModelTest {

    private val bread = IngredientInfo(101, "white_bread", null)
    private val milk = IngredientInfo(201, "cow_milk", null)
    private val now = Instant.parse("2026-11-17T12:00:00Z")

    private class FakeRepository(private val ingredients: List<IngredientInfo>) : MealRepository {
        val saved = mutableListOf<Triple<Long, Int, List<MealItem>>>()
        val deleted = mutableListOf<Long>()
        var allergyIds = emptySet<Long>()

        override suspend fun ingredients() = ingredients
        override suspend fun synonyms(language: String) = mapOf(101L to listOf("Semmel"))
        override suspend fun recentIngredientIds(limit: Int) = saved.flatMap { it.third }.map { it.ingredientId }.distinct().reversed()
        override suspend fun ingredientIdsWithKnownAllergy() = allergyIds
        var preparationIds = emptySet<Long>()
        override suspend fun ingredientIdsNeedingPreparation() = preparationIds
        val mealFavoriteIds = mutableListOf<Long?>()
        val slots = mutableListOf<MealSlot?>()
        val favorites = mutableListOf<FavoriteInfo>()

        override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?, slot: MealSlot?): Long {
            saved += Triple(eatenAt, tzOffsetMin, items)
            mealFavoriteIds += favoriteId
            slots += slot
            return saved.size.toLong()
        }
        override suspend fun deleteMeal(mealId: Long) {
            deleted += mealId
        }
        override suspend fun favorites() = favorites.sortedBy { it.name }
        override suspend fun saveFavorite(name: String, items: List<MealItem>): Long {
            val id = 50L + favorites.size
            favorites += FavoriteInfo(id, name, items)
            return id
        }
        override suspend fun deleteFavorite(favoriteId: Long) {
            favorites.removeAll { it.id == favoriteId }
        }

        // Meal id = position in [saved] + 1.
        override suspend fun meal(mealId: Long) =
            saved.getOrNull(mealId.toInt() - 1)?.let { LoggedMeal(mealId, it.first, it.third, mealFavoriteIds[mealId.toInt() - 1], slot = slots.getOrNull(mealId.toInt() - 1)) }
        override suspend fun updateMeal(mealId: Long, eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?, slot: MealSlot?) {
            saved[mealId.toInt() - 1] = Triple(eatenAt, tzOffsetMin, items)
            mealFavoriteIds[mealId.toInt() - 1] = favoriteId
            if (mealId.toInt() - 1 < slots.size) slots[mealId.toInt() - 1] = slot
        }
        override suspend fun mealIdOnDay(from: Long, until: Long, slot: MealSlot) = saved.indexOfFirst { it.first >= from && it.first < until && slots.getOrNull(saved.indexOf(it)) == slot }.takeIf { it >= 0 }?.let { it + 1L }
    }

    @Test
    fun dishGetsOnePortionForTheWholeDish() {
        repository.favorites += FavoriteInfo(7, "Breakfast", listOf(MealItem(bread.id, Portion.SMALL), MealItem(milk.id, Portion.LARGE)))
        val vm = viewModel()
        vm.onToggleIngredient(bread)

        vm.onFavoriteClick(vm.state.value.favorites.single())

        assertEquals(listOf(SelectedIngredient(bread)), vm.state.value.selected)
        // The dish starts at the largest portion of its main part and counts as a whole.
        assertEquals(listOf(ChosenDish(7, Portion.LARGE)), vm.state.value.dishes)
        assertEquals(listOf(MealItem(bread.id, Portion.LARGE), MealItem(milk.id, Portion.LARGE)), vm.state.value.mealItems)
        vm.onSlotChange(MealSlot.BREAKFAST)
        vm.onSave()
        assertEquals(7L, repository.mealFavoriteIds.single())
    }

    @Test
    fun voiceResultSelectsMatchesAndSearchesTheRest() {
        val vm = viewModel()

        vm.onVoiceResult("Semmel und Kuhmilch und Xylophon")

        assertEquals(listOf(bread, milk), vm.state.value.selected.map { it.ingredient })
        assertEquals("xylophon", vm.state.value.query)
    }

    @Test
    fun mealCanBeSavedAsNewFavorite() {
        val vm = viewModel()
        vm.onToggleIngredient(milk)
        vm.onSlotChange(MealSlot.SNACK)
        vm.onSaveAsFavoriteChange(true)

        assertFalse(vm.state.value.canSave)
        vm.onFavoriteNameChange(" Latte ")
        vm.onSave()

        assertEquals(FavoriteInfo(50, "Latte", listOf(MealItem(milk.id, Portion.MEDIUM))), repository.favorites.single())
        assertEquals(50L, repository.mealFavoriteIds.single())
        assertEquals(listOf("Latte"), vm.state.value.favorites.map { it.name })
        assertFalse(vm.state.value.saveAsFavorite)
    }

    @Test
    fun takingTheDishOutForgetsTheFavorite() {
        repository.favorites += FavoriteInfo(7, "Milk", listOf(MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()
        vm.onFavoriteClick(vm.state.value.favorites.single())

        vm.onRemoveDish(7)

        assertNull(vm.state.value.favoriteId)
        assertTrue(vm.state.value.mealItems.isEmpty())
    }

    @Test
    fun anIngredientOfTwoDishesIsStoredOnceWithTheLargerPortion() {
        repository.favorites += FavoriteInfo(7, "Brot", listOf(MealItem(bread.id, Portion.SMALL)))
        repository.favorites += FavoriteInfo(8, "Brot mit Milch", listOf(MealItem(bread.id, Portion.LARGE), MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()

        vm.onFavoriteClick(vm.state.value.favorites.first { it.id == 7L })
        vm.onFavoriteClick(vm.state.value.favorites.first { it.id == 8L })

        assertEquals(listOf(7L, 8L), vm.state.value.dishes.map { it.favoriteId })
        assertEquals(listOf(MealItem(bread.id, Portion.LARGE), MealItem(milk.id, Portion.LARGE)), vm.state.value.mealItems)
    }

    @Test
    fun removingADishKeepsWhatAnotherDishOrTheUserAdded() {
        repository.favorites += FavoriteInfo(7, "Brot", listOf(MealItem(bread.id, Portion.SMALL)))
        repository.favorites += FavoriteInfo(8, "Brot mit Milch", listOf(MealItem(bread.id, Portion.LARGE), MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()
        vm.onToggleIngredient(coffee)
        vm.onFavoriteClick(vm.state.value.favorites.first { it.id == 7L })
        vm.onFavoriteClick(vm.state.value.favorites.first { it.id == 8L })

        vm.onRemoveDish(8)

        assertEquals(setOf(coffee.id, bread.id), vm.state.value.mealItems.map { it.ingredientId }.toSet())
        assertEquals(7L, vm.state.value.favoriteId)
        // A second tap on the chosen dish takes it out too.
        vm.onFavoriteClick(vm.state.value.favorites.first { it.id == 7L })
        assertEquals(listOf(coffee.id), vm.state.value.mealItems.map { it.ingredientId })
        assertNull(vm.state.value.favoriteId)
    }

    @Test
    fun tappingTheChosenMealTakesItBack() {
        val vm = viewModel()
        vm.onSlotTap(MealSlot.LUNCH)
        assertEquals(MealSlot.LUNCH, vm.state.value.slot)

        vm.onSlotTap(MealSlot.LUNCH)

        assertNull(vm.state.value.slot)
        assertNull(vm.state.value.shownTime)
    }

    @Test
    fun choosingAnAlreadyLoggedLunchOpensIt() {
        val vm = viewModel()
        vm.onToggleIngredient(bread)
        vm.onSlotChange(MealSlot.LUNCH)
        vm.onSave()
        vm.onToggleIngredient(milk)

        vm.onSlotTap(MealSlot.LUNCH)

        assertEquals(1L, vm.state.value.editingMealId)
        assertEquals(MealSlot.LUNCH, vm.state.value.openedExisting)
        assertEquals(listOf(bread, milk), vm.state.value.selected.map { it.ingredient })
        // Another meal instead: back to a new one, without the lunch's ingredients.
        vm.onSlotTap(MealSlot.DINNER)
        assertNull(vm.state.value.editingMealId)
        assertEquals(listOf(milk), vm.state.value.selected.map { it.ingredient })
    }

    private val linseed = IngredientInfo(1201, "linseed", null, 9L)

    @Test
    fun seedsInADishCountOneStepSmallerThanItsMainPart() {
        repository.favorites += FavoriteInfo(7, "Sauerteigbrot", listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(linseed.id, Portion.MEDIUM)))
        val vm = viewModel()

        vm.onFavoriteClick(vm.state.value.favorites.single())
        assertEquals(listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(linseed.id, Portion.SMALL)), vm.state.value.mealItems)

        vm.onDishPortionChange(7, Portion.LARGE)
        assertEquals(listOf(MealItem(bread.id, Portion.LARGE), MealItem(linseed.id, Portion.MEDIUM)), vm.state.value.mealItems)
        vm.onDishPortionChange(7, Portion.SMALL)
        assertEquals(listOf(MealItem(bread.id, Portion.SMALL), MealItem(linseed.id, Portion.SMALL)), vm.state.value.mealItems)
    }

    @Test
    fun aDishOfOnlySeedsIsNotShrunk() {
        repository.favorites += FavoriteInfo(7, "Saatenmix", listOf(MealItem(linseed.id, Portion.MEDIUM)))
        val vm = viewModel()

        vm.onFavoriteClick(vm.state.value.favorites.single())

        assertEquals(listOf(MealItem(linseed.id, Portion.MEDIUM)), vm.state.value.mealItems)
    }

    @Test
    fun anIngredientAlreadyInADishAsksBeforeItIsAddedOnItsOwn() {
        repository.favorites += FavoriteInfo(7, "Brot mit Milch", listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()
        vm.onFavoriteClick(vm.state.value.favorites.single())

        vm.onToggleIngredient(milk)

        assertEquals(PendingAdd(milk, listOf(7L)), vm.state.value.pendingAdd)
        assertTrue(vm.state.value.selected.isEmpty())

        // "Nein, bereits enthaltene verwenden": nothing is added.
        vm.onDeclineAdd()
        assertNull(vm.state.value.pendingAdd)
        assertTrue(vm.state.value.selected.isEmpty())

        // "Ja, zusätzlich hinzufügen": it is also a single ingredient, the meal still stores it once.
        vm.onToggleIngredient(milk)
        vm.onConfirmAddAnyway()
        assertEquals(listOf(SelectedIngredient(milk)), vm.state.value.selected)
        assertEquals(listOf(bread.id, milk.id), vm.state.value.mealItems.map { it.ingredientId })
    }

    @Test
    fun removingAnIngredientFromOnePlaceNeedsNoQuestion() {
        repository.favorites += FavoriteInfo(7, "Brot mit Milch", listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()
        vm.onFavoriteClick(vm.state.value.favorites.single())

        vm.onRemoveIngredient(milk.id, 7)

        assertNull(vm.state.value.pendingRemove)
        assertEquals(listOf(bread.id), vm.state.value.mealItems.map { it.ingredientId })
        // The saved dish itself is unchanged.
        assertEquals(2, vm.state.value.favorites.single().items.size)
    }

    @Test
    fun anIngredientInSeveralPlacesAsksWhereToRemoveIt() {
        repository.favorites += FavoriteInfo(7, "Brot mit Milch", listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()
        vm.onFavoriteClick(vm.state.value.favorites.single())
        vm.onToggleIngredient(milk)
        vm.onConfirmAddAnyway()

        vm.onRemoveIngredient(milk.id, null)
        assertEquals(listOf(Place.Single, Place.InDish(7)), vm.state.value.pendingRemove?.places)

        // "Nur dieses Vorkommen": only the single one goes, the dish keeps it.
        vm.onRemoveHereOnly()
        assertNull(vm.state.value.pendingRemove)
        assertTrue(vm.state.value.selected.isEmpty())
        assertEquals(listOf(bread.id, milk.id), vm.state.value.mealItems.map { it.ingredientId })
    }

    @Test
    fun removingEverywhereTakesTheIngredientOutOfDishAndSingles() {
        repository.favorites += FavoriteInfo(7, "Brot mit Milch", listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()
        vm.onFavoriteClick(vm.state.value.favorites.single())
        vm.onToggleIngredient(milk)
        vm.onConfirmAddAnyway()

        vm.onRemoveIngredient(milk.id, 7)
        vm.onRemoveEverywhere()

        assertTrue(vm.state.value.selected.isEmpty())
        assertEquals(listOf(bread.id), vm.state.value.mealItems.map { it.ingredientId })
    }

    @Test
    fun cancellingTheRemoveQuestionChangesNothing() {
        repository.favorites += FavoriteInfo(7, "Brot mit Milch", listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()
        vm.onFavoriteClick(vm.state.value.favorites.single())
        vm.onToggleIngredient(milk)
        vm.onConfirmAddAnyway()
        vm.onRemoveIngredient(milk.id, null)

        vm.onRemoveCancelled()

        assertNull(vm.state.value.pendingRemove)
        assertEquals(listOf(SelectedIngredient(milk)), vm.state.value.selected)
    }

    @Test
    fun aDishWithoutIngredientsLeftIsRemoved() {
        repository.favorites += FavoriteInfo(7, "Milchkaffee", listOf(MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()
        vm.onFavoriteClick(vm.state.value.favorites.single())

        vm.onRemoveIngredient(milk.id, 7)

        assertTrue(vm.state.value.dishes.isEmpty())
        assertNull(vm.state.value.favoriteId)
        assertFalse(vm.state.value.canSave)
    }

    private val coffee = IngredientInfo(1101, "coffee", null, BEVERAGES_CATEGORY_ID)
    private val repository = FakeRepository(listOf(bread, milk, coffee, linseed))

    private fun viewModel() = LogMealViewModel(
        repository = repository,
        clock = AppClock(clock = object : Clock { override fun now() = now }, zone = { TimeZone.of("Europe/Berlin") }),
        names = { _, key ->
            when (key) {
                "white_bread" -> "Weißbrot"
                "coffee" -> "Kaffee"
                "linseed" -> "Leinsamen"
                else -> "Kuhmilch"
            }
        },
        language = { "de" },
    )

    @Test
    fun drinkKeepsItsKindWhenTheTimeChanges() {
        // Feedback 20.11.: coffee at 11:00 must not turn into "Mahlzeit".
        val vm = viewModel()
        vm.onToggleIngredient(coffee)
        vm.onSlotChange(MealSlot.DRINK)
        vm.onExactTimeChange(ExactTime(0, 11, 0))

        vm.onSave()

        assertEquals(MealSlot.DRINK, repository.slots.single())
        // 11:00 in Berlin = 10:00 UTC
        assertEquals(Instant.parse("2026-11-17T10:00:00Z").toEpochMilliseconds(), repository.saved.single().first)
    }

    @Test
    fun breakfastKeepsItsKindWithAnotherTime() {
        val vm = viewModel()
        vm.onToggleIngredient(bread)
        vm.onSlotChange(MealSlot.BREAKFAST)
        vm.onExactTimeChange(ExactTime(0, 9, 30))

        vm.onSave()

        assertEquals(MealSlot.BREAKFAST, repository.slots.single())
        vm.startEditing(1)
        assertEquals(MealSlot.BREAKFAST, vm.state.value.slot)
        assertEquals(ExactTime(0, 9, 30), vm.state.value.shownTime)
    }

    @Test
    fun onlyDrinksPreselectDrinkAndFoodTakesItBack() {
        val vm = viewModel()
        vm.onToggleIngredient(coffee)
        assertEquals(MealSlot.DRINK, vm.state.value.slot)
        assertEquals(ExactTime(0, 13, 0), vm.state.value.shownTime)

        vm.onToggleIngredient(bread)
        assertNull(vm.state.value.slot)
        assertNull(vm.state.value.shownTime)
    }

    @Test
    fun aChosenSlotIsNotReplacedByTheDrinkSuggestion() {
        val vm = viewModel()
        vm.onSlotChange(MealSlot.BREAKFAST)
        vm.onToggleIngredient(coffee)
        assertEquals(MealSlot.BREAKFAST, vm.state.value.slot)

        // A confirmed drink stays when food is added.
        val other = viewModel()
        other.onToggleIngredient(coffee)
        other.onSlotChange(MealSlot.DRINK)
        other.onToggleIngredient(bread)
        assertEquals(MealSlot.DRINK, other.state.value.slot)
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun searchUsesDisplayNamesAndSynonyms() {
        val vm = viewModel()

        vm.onQueryChange("semmel")
        assertEquals(listOf(bread), vm.state.value.results)

        vm.onQueryChange("kuhm")
        assertEquals(listOf(milk), vm.state.value.results)
    }

    @Test
    fun selectingTwiceRemovesTheIngredientAgain() {
        val vm = viewModel()

        vm.onToggleIngredient(bread)
        assertEquals(listOf(SelectedIngredient(bread)), vm.state.value.selected)

        vm.onToggleIngredient(bread)
        assertTrue(vm.state.value.selected.isEmpty())
        assertFalse(vm.state.value.canSave)
    }

    @Test
    fun saveStoresTimePortionsAndOffset() {
        val vm = viewModel()
        vm.onToggleIngredient(bread)
        vm.onToggleIngredient(milk)
        vm.onPortionChange(milk.id, Portion.LARGE)
        vm.onSlotChange(MealSlot.BREAKFAST)

        vm.onSave()

        val (eatenAt, offset, items) = repository.saved.single()
        // Breakfast = 08:00 in Berlin = 07:00 UTC
        assertEquals(Instant.parse("2026-11-17T07:00:00Z").toEpochMilliseconds(), eatenAt)
        assertEquals(60, offset)
        assertEquals(listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(milk.id, Portion.LARGE)), items)
        assertEquals(1L, vm.state.value.savedMealId)
        assertTrue(vm.state.value.selected.isEmpty())
        // The next meal starts without a preselected slot again.
        assertNull(vm.state.value.slot)
        assertNull(vm.state.value.shownTime)
    }

    @Test
    fun pickedTimeIsUsedAndNeverInTheFuture() {
        val vm = viewModel()
        vm.onToggleIngredient(bread)
        vm.onDayChange(1)
        vm.onSlotChange(MealSlot.DINNER)
        vm.onExactTimeChange(ExactTime(daysAgo = 1, hour = 19, minute = 30))
        assertEquals(MealSlot.DINNER, vm.state.value.slot)
        vm.onSave()

        // Yesterday 19:30 in Berlin = 18:30 UTC
        assertEquals(Instant.parse("2026-11-16T18:30:00Z").toEpochMilliseconds(), repository.saved.last().first)
        assertNull(vm.state.value.exactTime)

        vm.onToggleIngredient(bread)
        // The day row stays on yesterday after saving; back to today.
        vm.onDayChange(0)
        vm.onSlotChange(MealSlot.SNACK)
        vm.onExactTimeChange(ExactTime(daysAgo = 0, hour = 23, minute = 0))
        vm.onSave()
        assertEquals(now.toEpochMilliseconds(), repository.saved.last().first)
    }

    @Test
    fun nothingIsPreselectedAndSavingAsksForTheSlot() {
        // Self-test 19.11.: the time-based preselection ("snack") led to wrong entries.
        val vm = viewModel()
        assertNull(vm.state.value.slot)

        vm.onToggleIngredient(bread)
        assertTrue(vm.state.value.canSave)
        vm.onSave()

        assertTrue(repository.saved.isEmpty())
        assertTrue(vm.state.value.slotMissing)

        vm.onSlotChange(MealSlot.LUNCH)
        assertFalse(vm.state.value.slotMissing)
    }

    @Test
    fun everySlotShowsTheTimeThatWillBeStored() {
        // 13:00 in Berlin.
        val vm = viewModel()

        vm.onSlotChange(MealSlot.BREAKFAST)
        assertEquals(ExactTime(0, 8, 0), vm.state.value.shownTime)
        vm.onSlotChange(MealSlot.DINNER)
        assertEquals(ExactTime(0, 13, 0), vm.state.value.shownTime)
        vm.onSlotChange(MealSlot.SNACK)
        assertEquals(ExactTime(0, 13, 0), vm.state.value.shownTime)

        vm.onDayChange(1)
        vm.onExactTimeChange(ExactTime(0, 16, 30))
        assertEquals(ExactTime(1, 16, 30), vm.state.value.shownTime)
        assertEquals(MealSlot.SNACK, vm.state.value.slot)
    }

    @Test
    fun slotTimeIsNeverInTheFuture() {
        // 13:00 in Berlin.
        val vm = viewModel()

        vm.onToggleIngredient(bread)
        vm.onSlotChange(MealSlot.LUNCH)
        vm.onSave()
        assertEquals(Instant.parse("2026-11-17T11:30:00Z").toEpochMilliseconds(), repository.saved.last().first)

        vm.onToggleIngredient(bread)
        vm.onSlotChange(MealSlot.DINNER)
        vm.onSave()
        assertEquals(now.toEpochMilliseconds(), repository.saved.last().first)

        vm.onToggleIngredient(bread)
        vm.onSlotChange(MealSlot.SNACK)
        vm.onSave()
        assertEquals(now.toEpochMilliseconds(), repository.saved.last().first)
    }

    @Test
    fun quickChipReplacesThePickedTimeButKeepsAnEarlierDay() {
        val vm = viewModel()
        vm.onSlotChange(MealSlot.BREAKFAST)
        vm.onExactTimeChange(ExactTime(0, 9, 15))
        vm.onSlotChange(MealSlot.LUNCH)
        assertNull(vm.state.value.exactTime)

        vm.onDayChange(1)
        vm.onExactTimeChange(ExactTime(0, 8, 0))
        vm.onSlotChange(MealSlot.DINNER)
        assertEquals(ExactTime(1, 19, 0), vm.state.value.shownTime)
    }

    @Test
    fun loggedMealIsEditedInPlace() {
        // Yesterday's dinner (19:00 Berlin = 18:00 UTC) with bread.
        val dinner = Instant.parse("2026-11-16T18:00:00Z").toEpochMilliseconds()
        repository.saved += Triple(dinner, 60, listOf(MealItem(bread.id, Portion.MEDIUM)))
        repository.mealFavoriteIds += null
        val vm = viewModel()

        vm.startEditing(1L)

        assertEquals(1L, vm.state.value.editingMealId)
        assertEquals(MealSlot.DINNER, vm.state.value.slot)
        assertEquals(ExactTime(1, 19, 0), vm.state.value.shownTime)
        assertEquals(listOf(SelectedIngredient(bread)), vm.state.value.selected)

        vm.onToggleIngredient(milk)
        vm.onSave()

        assertEquals(1, repository.saved.size)
        assertEquals(Triple(dinner, 60, listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(milk.id, Portion.MEDIUM))), repository.saved.single())
        assertTrue(vm.state.value.finished)
    }

    @Test
    fun secondLunchOfTheDayCanBeAddedToTheFirst() {
        // Self-test 19.11.: a forgotten ingredient gave a second entry for the same meal.
        val vm = viewModel()
        vm.onToggleIngredient(bread)
        vm.onPortionChange(bread.id, Portion.SMALL)
        vm.onSlotChange(MealSlot.LUNCH)
        vm.onSave()

        vm.onToggleIngredient(milk)
        vm.onToggleIngredient(bread)
        vm.onSlotChange(MealSlot.LUNCH)
        vm.onSave()

        assertEquals(1L, vm.state.value.existingMealId)
        assertEquals(1, repository.saved.size)

        vm.onAddToExisting()

        assertEquals(listOf(MealItem(milk.id, Portion.MEDIUM), MealItem(bread.id, Portion.MEDIUM)), repository.saved.single().third)
        assertEquals(MealSlot.LUNCH, vm.state.value.addedTo)
        assertTrue(vm.state.value.selected.isEmpty())
    }

    @Test
    fun secondLunchCanStillBeASeparateMeal() {
        val vm = viewModel()
        vm.onToggleIngredient(bread)
        vm.onSlotChange(MealSlot.LUNCH)
        vm.onSave()
        vm.onToggleIngredient(milk)
        vm.onSlotChange(MealSlot.LUNCH)
        vm.onSave()

        vm.onSaveSeparately()

        assertEquals(2, repository.saved.size)
        assertNull(vm.state.value.existingMealId)
    }

    @Test
    fun snacksAreNeverMerged() {
        val vm = viewModel()
        vm.onToggleIngredient(bread)
        vm.onSlotChange(MealSlot.SNACK)
        vm.onSave()
        vm.onToggleIngredient(milk)
        vm.onSlotChange(MealSlot.SNACK)
        vm.onSave()

        assertEquals(2, repository.saved.size)
    }

    @Test
    fun searchFindsDishesByIngredientAndPickingOneFillsTheForm() {
        repository.favorites += FavoriteInfo(7, "Frühstück", listOf(MealItem(bread.id, Portion.SMALL), MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()

        vm.onQueryChange("kuhm")
        assertEquals(listOf(7L), vm.state.value.dishResults.map { it.id })

        vm.onFavoriteClick(vm.state.value.dishResults.single())
        assertEquals(listOf(bread.id, milk.id), vm.state.value.mealItems.map { it.ingredientId })
        assertTrue(vm.state.value.dishResults.isEmpty())
        assertEquals("", vm.state.value.query)
    }

    @Test
    fun dishFromTheFavouritesStartsTheFormWithoutAMeal() {
        repository.favorites += FavoriteInfo(7, "Milchkaffee", listOf(MealItem(milk.id, Portion.LARGE)))
        val vm = viewModel()

        vm.startWithFavorite(7)

        assertEquals(listOf(ChosenDish(7, Portion.LARGE)), vm.state.value.dishes)
        assertEquals(listOf(MealItem(milk.id, Portion.LARGE)), vm.state.value.mealItems)
        assertEquals(7L, vm.state.value.favoriteId)
        // The meal is still chosen by the user.
        assertNull(vm.state.value.slot)
    }

    @Test
    fun mealSlotIsRecognisedByItsFixedTime() {
        assertEquals(MealSlot.BREAKFAST, MealSlot.at(8, 0))
        assertEquals(MealSlot.LUNCH, MealSlot.at(12, 30))
        assertEquals(MealSlot.DINNER, MealSlot.at(19, 0))
        assertNull(MealSlot.at(19, 1))
    }

    @Test
    fun savedMealShowsUpAsRecentAndCanBeUndone() {
        val vm = viewModel()
        vm.onToggleIngredient(milk)
        vm.onSlotChange(MealSlot.SNACK)
        vm.onSave()

        assertEquals(listOf(milk), vm.state.value.recent)

        vm.onUndo(1L)
        vm.onSavedShown()
        assertEquals(listOf(1L), repository.deleted)
        assertNull(vm.state.value.savedMealId)
    }

    @Test
    fun knownAllergyShowsAHintButSavingStaysPossible() {
        repository.allergyIds = setOf(milk.id)
        val vm = viewModel()

        vm.onToggleIngredient(milk)

        assertTrue(vm.state.value.containsKnownAllergy)
        assertTrue(vm.state.value.canSave)
    }

    @Test
    fun sameNameIsShownOnceInRecentAndNotCreatedAgain() {
        // Self-test 18.11.: a custom "Dinkelvollkornbrot" from before the seed entry existed.
        val seed = IngredientInfo(114, "spelt_wholemeal_bread", null)
        val custom = IngredientInfo(100_000, null, "Dinkelvollkornbrot")
        val repo = FakeRepository(listOf(seed, custom))
        repo.saved += Triple(0L, 60, listOf(MealItem(custom.id, Portion.MEDIUM)))
        repo.saved += Triple(1L, 60, listOf(MealItem(seed.id, Portion.MEDIUM)))
        val vm = LogMealViewModel(
            repository = repo,
            clock = AppClock(clock = object : Clock { override fun now() = now }, zone = { TimeZone.of("Europe/Berlin") }),
            names = { _, _ -> "Dinkelvollkornbrot" },
            language = { "de" },
        )

        assertEquals(1, vm.state.value.recent.size)

        vm.onQueryChange("dinkelvollkornbrot")
        assertTrue(vm.state.value.nameExists)
        vm.onQueryChange("Quinoa")
        assertFalse(vm.state.value.nameExists)
    }

    @Test
    fun rawOrCookedMustBeChosenAndIsSaved() {
        repository.preparationIds = setOf(milk.id)
        val vm = viewModel()
        vm.onToggleIngredient(bread)
        vm.onToggleIngredient(milk)
        vm.onSlotChange(MealSlot.LUNCH)

        assertTrue(vm.state.value.missingPreparation)
        assertFalse(vm.state.value.canSave)
        vm.onSave()
        assertTrue(repository.saved.isEmpty())

        vm.onPreparationChange(milk.id, Preparation.COOKED)
        assertTrue(vm.state.value.canSave)
        vm.onSave()

        assertEquals(
            listOf(MealItem(bread.id, Portion.MEDIUM, null), MealItem(milk.id, Portion.MEDIUM, Preparation.COOKED)),
            repository.saved.single().third,
        )
    }
}
