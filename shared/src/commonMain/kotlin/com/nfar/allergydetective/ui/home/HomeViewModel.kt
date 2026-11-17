package com.nfar.allergydetective.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.DayCheckRepository
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
)

/** Home dashboard (UX 2): detective progress from confirmed days and the quote of the day. */
class HomeViewModel(
    private val dayChecks: DayCheckRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    /** Called whenever the home screen becomes visible, e.g. after a day check. */
    fun refresh() {
        viewModelScope.launch {
            _state.value = HomeState(confirmedDays = dayChecks.confirmedDayCount(), today = clock.today())
        }
    }

    companion object {
        /** Days until the first evaluation (UX 5). */
        const val COLLECTION_DAYS = 7

        /** Same quote all day, a different one tomorrow; the pool repeats after [size] days. */
        fun quoteIndex(epochDay: Long, size: Int): Int = if (size == 0) 0 else epochDay.mod(size)
    }
}
