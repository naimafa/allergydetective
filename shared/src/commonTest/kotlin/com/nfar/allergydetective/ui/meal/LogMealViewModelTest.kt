package com.nfar.allergydetective.ui.meal

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.IngredientInfo
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
        val favorites = mutableListOf<FavoriteInfo>()

        override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?): Long {
            saved += Triple(eatenAt, tzOffsetMin, items)
            mealFavoriteIds += favoriteId
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
    }

    @Test
    fun favoriteAddsItsIngredientsWithTheirPortions() {
        repository.favorites += FavoriteInfo(7, "Breakfast", listOf(MealItem(bread.id, Portion.SMALL), MealItem(milk.id, Portion.LARGE)))
        val vm = viewModel()
        vm.onToggleIngredient(bread)

        vm.onFavoriteClick(vm.state.value.favorites.single())

        assertEquals(listOf(SelectedIngredient(bread), SelectedIngredient(milk, Portion.LARGE)), vm.state.value.selected)
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
    fun clearingTheSelectionForgetsTheFavorite() {
        repository.favorites += FavoriteInfo(7, "Milk", listOf(MealItem(milk.id, Portion.MEDIUM)))
        val vm = viewModel()
        vm.onFavoriteClick(vm.state.value.favorites.single())

        vm.onToggleIngredient(milk)

        assertNull(vm.state.value.favoriteId)
    }

    private val repository = FakeRepository(listOf(bread, milk))

    private fun viewModel() = LogMealViewModel(
        repository = repository,
        clock = AppClock(clock = object : Clock { override fun now() = now }, zone = { TimeZone.of("Europe/Berlin") }),
        names = { _, key -> if (key == "white_bread") "Weißbrot" else "Kuhmilch" },
        language = { "de" },
    )

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
        vm.onSlotChange(MealSlot.DINNER)
        vm.onExactTimeChange(ExactTime(daysAgo = 1, hour = 19, minute = 30))
        assertEquals(MealSlot.DINNER, vm.state.value.slot)
        vm.onSave()

        // Yesterday 19:30 in Berlin = 18:30 UTC
        assertEquals(Instant.parse("2026-11-16T18:30:00Z").toEpochMilliseconds(), repository.saved.last().first)
        assertNull(vm.state.value.exactTime)

        vm.onToggleIngredient(bread)
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

        vm.onExactTimeChange(ExactTime(1, 16, 30))
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
    fun quickChipReplacesThePickedTime() {
        val vm = viewModel()
        vm.onSlotChange(MealSlot.BREAKFAST)
        vm.onExactTimeChange(ExactTime(1, 8, 0))

        vm.onSlotChange(MealSlot.DINNER)

        assertNull(vm.state.value.exactTime)
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
