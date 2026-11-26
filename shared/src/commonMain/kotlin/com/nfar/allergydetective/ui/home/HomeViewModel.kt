package com.nfar.allergydetective.ui.home

import com.nfar.allergydetective.ui.meal.splitExamples
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.HistoryRepository
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.reminder.GoalSettings
import com.nfar.allergydetective.domain.reminder.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeState(
    /** Null while loading. */
    val confirmedDays: Int? = null,
    /** Local epoch day, picks the daily quote. */
    val today: Long = 0,
    /** Meals and symptoms logged today: "2 today" on the tiles (self-test 19.11.). */
    val mealsToday: Int = 0,
    val symptomsToday: Int = 0,
    /** Today is not confirmed yet: the day check tile shows "open" (self-test 18.11.). */
    val todayOpen: Boolean = false,
    /** Local hour, picks "good morning / day / evening" (home design of 21.11.2026). */
    val hour: Int = 12,
    /** Confirmed days the current goal needs (user 23.11.2026: set in the profile, 7 by default). */
    val goalDays: Int = GoalSettings.DEFAULT_DAYS,
    /** Optional focus of the stretch, e.g. "Milchprodukte weglassen". */
    val goalFocus: String = "",
) {
    val greeting: Greeting get() = HomeViewModel.greeting(hour)
}

enum class Greeting { MORNING, DAY, EVENING }

/** Home dashboard (UX 2): detective progress from confirmed days and the quote of the day. */
class HomeViewModel(
    private val dayChecks: DayCheckRepository,
    private val history: HistoryRepository,
    private val clock: AppClock,
    private val settings: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    /** The goal is reached: a new stretch counts from today on (user 23.11.2026: "was kommt danach?"). */
    fun onStartNewStretch() {
        settings.saveGoal(settings.goal().copy(startDay = clock.today()))
        refresh()
    }

    /** Called whenever the home screen becomes visible, e.g. after a day check. */
    fun refresh() {
        viewModelScope.launch {
            val today = clock.today()
            val entries = history.entriesSince(clock.startOfDayMillis(today))
            val goal = settings.goal()
            _state.value = HomeState(
                // Only the days of the current stretch count; a new stretch starts with "Neuen Abschnitt starten".
                confirmedDays = goal.startDay?.let { dayChecks.confirmedDayCountSince(it) } ?: dayChecks.confirmedDayCount(),
                goalDays = goal.days,
                goalFocus = splitExamples(goal.focus).first, // older tests saved the long name with examples
                today = today,
                todayOpen = dayChecks.load(today)?.isConfirmed != true,
                mealsToday = entries.count { it is HistoryEntry.MealEntry },
                symptomsToday = entries.count { it is HistoryEntry.SymptomEntry },
                hour = clock.localTime(clock.nowMillis()).first,
            )
        }
    }

    companion object {
        /** Days until the first evaluation (UX 5); the goal in the profile can change it. */
        const val COLLECTION_DAYS = 7

        /** Same quote all day, a different one tomorrow; the pool repeats after [size] days. */
        /** Morning from 5 to 10, day from 11 to 17, evening (and night) from 18 to 4. */
        fun greeting(hour: Int): Greeting = when (hour) {
            in 5..10 -> Greeting.MORNING
            in 11..17 -> Greeting.DAY
            else -> Greeting.EVENING
        }

        fun quoteIndex(epochDay: Long, size: Int): Int = if (size == 0) 0 else epochDay.mod(size)
    }
}
