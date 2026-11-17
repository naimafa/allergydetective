package com.nfar.allergydetective.ui.meal

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.time.AppClock
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
        override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>): Long {
            saved += Triple(eatenAt, tzOffsetMin, items)
            return saved.size.toLong()
        }
        override suspend fun deleteMeal(mealId: Long) {
            deleted += mealId
        }
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
        vm.onHoursAgoChange(2)

        vm.onSave()

        val (eatenAt, offset, items) = repository.saved.single()
        assertEquals(now.toEpochMilliseconds() - 2 * 3_600_000L, eatenAt)
        assertEquals(60, offset)
        assertEquals(listOf(MealItem(bread.id, Portion.MEDIUM), MealItem(milk.id, Portion.LARGE)), items)
        assertEquals(1L, vm.state.value.savedMealId)
        assertTrue(vm.state.value.selected.isEmpty())
        assertEquals(0, vm.state.value.hoursAgo)
    }

    @Test
    fun pickedTimeIsUsedAndNeverInTheFuture() {
        val vm = viewModel()
        vm.onToggleIngredient(bread)
        vm.onExactTimeChange(ExactTime(daysAgo = 1, hour = 19, minute = 30))
        vm.onSave()

        // Yesterday 19:30 in Berlin = 18:30 UTC
        assertEquals(Instant.parse("2026-11-16T18:30:00Z").toEpochMilliseconds(), repository.saved.last().first)
        assertNull(vm.state.value.exactTime)

        vm.onToggleIngredient(bread)
        vm.onExactTimeChange(ExactTime(daysAgo = 0, hour = 23, minute = 0))
        vm.onSave()
        assertEquals(now.toEpochMilliseconds(), repository.saved.last().first)
    }

    @Test
    fun quickChipReplacesThePickedTime() {
        val vm = viewModel()
        vm.onExactTimeChange(ExactTime(1, 8, 0))

        vm.onHoursAgoChange(1)

        assertNull(vm.state.value.exactTime)
    }

    @Test
    fun savedMealShowsUpAsRecentAndCanBeUndone() {
        val vm = viewModel()
        vm.onToggleIngredient(milk)
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
}
