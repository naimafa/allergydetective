package com.nfar.allergydetective.ui.history

import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.HistoryRepository
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.SymptomInfo
import com.nfar.allergydetective.domain.diary.SymptomRepository
import com.nfar.allergydetective.domain.time.AppClock
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HistoryViewModelTest {

    private val clock = AppClock(
        clock = object : Clock { override fun now() = Instant.parse("2026-11-17T12:00:00Z") },
        zone = { TimeZone.of("Europe/Berlin") },
    )
    private val today = clock.today()
    private val milk = IngredientInfo(201, "cow_milk", null)

    private val entries = mutableListOf<HistoryEntry>(
        HistoryEntry.MealEntry(1, clock.millisAt(today, 8, 0), listOf(milk to Portion.MEDIUM)),
        HistoryEntry.SymptomEntry(2, clock.millisAt(today, 9, 30), "bloating", 4, null),
        // 00:30 local is still "yesterday" in UTC, but belongs to today
        HistoryEntry.MealEntry(3, clock.millisAt(today, 0, 30), emptyList()),
        HistoryEntry.MealEntry(4, clock.millisAt(today - 3, 12, 0), emptyList()),
    )
    private val deletedMeals = mutableListOf<Long>()
    private val deletedSymptoms = mutableListOf<Long>()

    private val history = object : HistoryRepository {
        override suspend fun entriesSince(fromMillis: Long) = entries.filter { it.time >= fromMillis }.sortedByDescending { it.time }
        override suspend fun dayStatusSince(fromDay: Long) = mapOf(today - 1 to DayCheckStatus.NO_SYMPTOMS)
    }

    private val meals = object : MealRepository {
        override suspend fun ingredients() = listOf(milk)
        override suspend fun synonyms(language: String) = emptyMap<Long, List<String>>()
        override suspend fun recentIngredientIds(limit: Int) = emptyList<Long>()
        override suspend fun ingredientIdsWithKnownAllergy() = emptySet<Long>()
        override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?) = 0L
        override suspend fun deleteMeal(mealId: Long) {
            deletedMeals += mealId
            entries.removeAll { it is HistoryEntry.MealEntry && it.id == mealId }
        }
        override suspend fun favorites() = emptyList<FavoriteInfo>()
        override suspend fun saveFavorite(name: String, items: List<MealItem>) = 0L
        override suspend fun deleteFavorite(favoriteId: Long) = Unit
    }

    private val symptoms = object : SymptomRepository {
        override suspend fun symptoms() = emptyList<SymptomInfo>()
        override suspend fun mostUsedSymptomIds(limit: Int) = emptyList<Long>()
        override suspend fun saveEntry(symptomId: Long, startedAt: Long, tzOffsetMin: Int, intensity: Int, note: String?) = 0L
        override suspend fun deleteEntry(entryId: Long) {
            deletedSymptoms += entryId
            entries.removeAll { it is HistoryEntry.SymptomEntry && it.id == entryId }
        }
    }

    private fun viewModel() = HistoryViewModel(history, meals, symptoms, clock).also { it.refresh() }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun entriesAreGroupedByLocalDayNewestFirst() {
        val days = viewModel().state.value.days

        assertEquals(listOf(today, today - 1, today - 3), days.map { it.epochDay })
        assertEquals(listOf(2L, 1L, 3L), days[0].entries.map { it.id })
        assertEquals(DayCheckStatus.NO_SYMPTOMS, days[1].status)
        assertEquals(emptyList(), days[1].entries)
    }

    @Test
    fun deletingAMealOrSymptomUsesTheRightRepository() {
        val vm = viewModel()

        vm.onDelete(vm.state.value.days[0].entries.first { it.id == 2L })
        vm.onDelete(vm.state.value.days[0].entries.first { it.id == 1L })

        assertEquals(listOf(2L), deletedSymptoms)
        assertEquals(listOf(1L), deletedMeals)
        assertEquals(listOf(3L), vm.state.value.days[0].entries.map { it.id })
    }
}
