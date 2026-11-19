package com.nfar.allergydetective.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.HistoryRepository
import com.nfar.allergydetective.domain.time.AppClock
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
)

/** Home dashboard (UX 2): detective progress from confirmed days and the quote of the day. */
class HomeViewModel(
    private val dayChecks: DayCheckRepository,
    private val history: HistoryRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    /** Called whenever the home screen becomes visible, e.g. after a day check. */
    fun refresh() {
        viewModelScope.launch {
            val today = clock.today()
            val entries = history.entriesSince(clock.startOfDayMillis(today))
            _state.value = HomeState(
                confirmedDays = dayChecks.confirmedDayCount(),
                today = today,
                todayOpen = dayChecks.load(today)?.isConfirmed != true,
                mealsToday = entries.count { it is HistoryEntry.MealEntry },
                symptomsToday = entries.count { it is HistoryEntry.SymptomEntry },
            )
        }
    }

    companion object {
        /** Days until the first evaluation (UX 5). */
        const val COLLECTION_DAYS = 7

        /** Same quote all day, a different one tomorrow; the pool repeats after [size] days. */
        fun quoteIndex(epochDay: Long, size: Int): Int = if (size == 0) 0 else epochDay.mod(size)
    }
}
