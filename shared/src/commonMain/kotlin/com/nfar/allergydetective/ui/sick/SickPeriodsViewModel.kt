package com.nfar.allergydetective.ui.sick

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.SickPeriod
import com.nfar.allergydetective.domain.diary.SickPeriodRepository
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SickPeriodsState(
    val today: Long = 0,
    /** Newest first. */
    val periods: List<SickPeriod> = emptyList(),
) {
    /** The period without an end, shown on the home screen with "well again". */
    val ongoing: SickPeriod? get() = periods.firstOrNull { it.endDay == null }

    fun isSick(day: Long): Boolean = periods.any { it.contains(day, today) }
}

/**
 * Illness over several days (self-test 18.11.: e.g. sinusitis with headache and nausea).
 * Entered once as a period instead of marking every day; such days do not count.
 */
class SickPeriodsViewModel(
    private val repository: SickPeriodRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(SickPeriodsState())
    val state: StateFlow<SickPeriodsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { _state.value = SickPeriodsState(today = clock.today(), periods = repository.periods()) }
    }

    /** [endDay] null = still ill. Days in the future are not allowed; the end is never before the start. */
    fun onAdd(startDay: Long, endDay: Long?) {
        val today = clock.today()
        val start = minOf(startDay, today)
        val end = endDay?.coerceIn(start, today)
        viewModelScope.launch {
            // Only one open period: a new open one closes the old one the day before it starts.
            if (end == null) {
                _state.value.ongoing?.let { repository.setEnd(it.id, maxOf(it.startDay, start - 1)) }
            }
            repository.add(start, end)
            refresh()
        }
    }

    /** Corrects a period afterwards, e.g. from the history; same limits as [onAdd]. */
    fun onUpdate(id: Long, startDay: Long, endDay: Long?) {
        val today = clock.today()
        val start = minOf(startDay, today)
        val end = endDay?.coerceIn(start, today)
        viewModelScope.launch {
            // Still only one open period.
            if (end == null) {
                _state.value.periods.firstOrNull { it.endDay == null && it.id != id }
                    ?.let { repository.setEnd(it.id, maxOf(it.startDay, start - 1)) }
            }
            repository.update(id, start, end)
            refresh()
        }
    }

    /** "Well again": today counts as healthy, unless the illness only started today. */
    fun onRecovered() {
        val ongoing = _state.value.ongoing ?: return
        val today = clock.today()
        viewModelScope.launch {
            repository.setEnd(ongoing.id, maxOf(ongoing.startDay, today - 1))
            refresh()
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            refresh()
        }
    }
}
