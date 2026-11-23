package com.nfar.allergydetective.ui.home

import com.nfar.allergydetective.domain.diary.DayCheckData
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.FactorInfo
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.HistoryRepository
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.reminder.GoalSettings
import com.nfar.allergydetective.domain.reminder.ReminderSettings
import com.nfar.allergydetective.domain.reminder.SettingsStore
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
        override suspend fun confirmedDayCountSince(fromDay: Long) = checks.values.count { it.isConfirmed && it.epochDay >= fromDay }
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

   private val settings = object : SettingsStore {
        var stored = GoalSettings()
        override fun reminder() = ReminderSettings()
        override fun saveReminder(settings: ReminderSettings) = Unit
        override fun goal() = stored
        override fun saveGoal(goal: GoalSettings) {
            stored = goal
        }
    }

    private fun viewModel() = HomeViewModel(repository, history, clock, settings)

    private fun refreshedState(): HomeState = viewModel().apply { refresh() }.state.value

    @Test
    fun theGoalFromTheProfileIsCountedAndANewStretchStartsToday() {
        (1..3).forEach { repository.checks[today - it] = DayCheckData(epochDay = today - it, status = DayCheckStatus.COMPLETE) }
        settings.stored = GoalSettings(days = 3, focus = "Milch weglassen")
        val vm = viewModel().apply { refresh() }
        assertEquals(3, vm.state.value.confirmedDays)
        assertEquals(3, vm.state.value.goalDays)
        assertEquals("Milch weglassen", vm.state.value.goalFocus)

        vm.onStartNewStretch()

        assertEquals(today, settings.stored.startDay)
        assertEquals(0, vm.state.value.confirmedDays)
    }

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
    fun localHourPicksTheGreeting() {
        // 19:00 UTC is 20:00 in Berlin.
        assertEquals(20, refreshedState().hour)
        assertEquals(Greeting.EVENING, refreshedState().greeting)
    }

    @Test
    fun greetingBoundaries() {
        assertEquals(Greeting.EVENING, HomeViewModel.greeting(4))
        assertEquals(Greeting.MORNING, HomeViewModel.greeting(5))
        assertEquals(Greeting.MORNING, HomeViewModel.greeting(10))
        assertEquals(Greeting.DAY, HomeViewModel.greeting(11))
        assertEquals(Greeting.DAY, HomeViewModel.greeting(17))
        assertEquals(Greeting.EVENING, HomeViewModel.greeting(18))
        assertEquals(Greeting.EVENING, HomeViewModel.greeting(0))
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
