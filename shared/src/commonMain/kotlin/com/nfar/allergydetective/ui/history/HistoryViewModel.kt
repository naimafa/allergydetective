package com.nfar.allergydetective.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.HistoryRepository
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.SymptomRepository
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryDay(
    val epochDay: Long,
    val status: DayCheckStatus?,
    val entries: List<HistoryEntry>,
)

data class HistoryState(
    val loading: Boolean = true,
    /** Newest day first. Days without entries and without a check are left out. */
    val days: List<HistoryDay> = emptyList(),
)

/** History of the last [DAYS] days with deletion of single entries. */
class HistoryViewModel(
    private val history: HistoryRepository,
    private val meals: MealRepository,
    private val symptoms: SymptomRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val today = clock.today()
            val fromDay = today - DAYS + 1
            val entries = history.entriesSince(clock.startOfDayMillis(fromDay))
            val status = history.dayStatusSince(fromDay)
            val byDay = entries.groupBy { clock.epochDayOf(it.time) }
            val days = (today downTo fromDay)
                .filter { it in byDay || it in status }
                // Within a day in diary order: morning first.
                .map { HistoryDay(it, status[it], byDay[it].orEmpty().sortedBy { entry -> entry.time }) }
            _state.value = HistoryState(loading = false, days = days)
        }
    }

    /** Permanently removes the entry; the screen asks first. */
    fun onDelete(entry: HistoryEntry) {
        viewModelScope.launch {
            when (entry) {
                is HistoryEntry.MealEntry -> meals.deleteMeal(entry.id)
                is HistoryEntry.SymptomEntry -> symptoms.deleteEntry(entry.id)
            }
            refresh()
        }
    }

    companion object {
        const val DAYS = 14
    }
}
