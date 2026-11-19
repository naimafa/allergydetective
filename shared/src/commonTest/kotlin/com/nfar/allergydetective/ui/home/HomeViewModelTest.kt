package com.nfar.allergydetective.ui.home

import com.nfar.allergydetective.domain.diary.DayCheckData
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.FactorInfo
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.HistoryRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HomeViewModelTest {

    private val clock = AppClock(
        clock = object : Clock { override fun now() = Instant.parse("2026-11-19T19:00:00Z") },
        zone = { TimeZone.of("Europe/Berlin") },
    )
    private val today = clock.today()

    private class FakeRepository : DayCheckRepository {
        val checks = mutableMapOf<Long, DayCheckData>()

        override suspend fun load(epochDay: Long) = checks[epochDay]
        override suspend fun lifestyleFactors() = emptyList<FactorInfo>()
        override suspend fun symptomCount(fromMillis: Long, toMillis: Long) = 0
        override suspend fun save(check: DayCheckData) {
            checks[check.epochDay] = check
        }
        override suspend fun confirmedDays(days: List<Long>) = days.filter { checks[it]?.isConfirmed == true }.toSet()
        override suspend fun confirmedDayCount() = checks.values.count { it.isConfirmed }
    }

    private val repository = FakeRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val entries = mutableListOf<HistoryEntry>()
    private val history = object : HistoryRepository {
        override suspend fun entriesSince(fromMillis: Long) = entries.filter { it.time >= fromMillis }
    }

    private fun refreshedState(): HomeState = HomeViewModel(repository, history, clock).apply { refresh() }.state.value

    @Test
    fun todaysMealsAndSymptomsAreCounted() {
        entries += HistoryEntry.MealEntry(1, clock.millisAt(today, 8, 0), emptyList())
        entries += HistoryEntry.MealEntry(2, clock.millisAt(today, 12, 30), emptyList())
        entries += HistoryEntry.MealEntry(3, clock.millisAt(today - 1, 19, 0), emptyList())
        entries += HistoryEntry.SymptomEntry(4, clock.millisAt(today, 14, 0), "bloating", 3, null)

        val state = refreshedState()

        assertEquals(2, state.mealsToday)
        assertEquals(1, state.symptomsToday)
    }

    @Test
    fun todayIsOpenUntilItIsConfirmed() {
        repository.checks[today - 1] = DayCheckData(today - 1, status = DayCheckStatus.COMPLETE)
        repository.checks[today] = DayCheckData(today, sleepHours = 7f)

        val state = refreshedState()

        assertTrue(state.todayOpen)
        assertEquals(1, state.confirmedDays)
    }

    @Test
    fun confirmedTodayIsNotOpen() {
        repository.checks[today] = DayCheckData(today, status = DayCheckStatus.NO_SYMPTOMS)

        assertFalse(refreshedState().todayOpen)
    }

    @Test
    fun quoteChangesDailyAndRepeatsAfterThePool() {
        assertEquals(0, HomeViewModel.quoteIndex(20_400, 40))
        assertEquals(1, HomeViewModel.quoteIndex(20_401, 40))
        assertEquals(0, HomeViewModel.quoteIndex(20_440, 40))
    }

    @Test
    fun emptyPoolDoesNotCrash() {
        assertEquals(0, HomeViewModel.quoteIndex(20_401, 0))
    }
}
