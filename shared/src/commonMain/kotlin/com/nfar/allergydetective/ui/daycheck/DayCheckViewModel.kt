package com.nfar.allergydetective.ui.daycheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.DayCheckData
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.FactorInfo
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DayCheckState(
    val loading: Boolean = true,
    val today: Long = 0,
    /** Today and up to two days back (UX 3.4), newest first. */
    val days: List<Long> = emptyList(),
    val confirmedDays: Set<Long> = emptySet(),
    val check: DayCheckData = DayCheckData(epochDay = 0),
    val factors: List<FactorInfo> = emptyList(),
    /** Symptoms were logged on this day, so "no complaints" is not offered. */
    val hasSymptoms: Boolean = false,
    /** Set after saving, the screen closes then. */
    val saved: Boolean = false,
) {
    val canChooseNoSymptoms: Boolean get() = !hasSymptoms
}

/** Day check (UX 3.4): wellbeing, sleep, stress, factors and the confirmation status. */
class DayCheckViewModel(
    private val repository: DayCheckRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(DayCheckState())
    val state: StateFlow<DayCheckState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val today = clock.today()
            val days = (0..DAYS_BACK).map { today - it }
            val confirmed = repository.confirmedDays(days)
            // Offer the oldest unconfirmed day first, otherwise today (UX 3.4).
            val start = days.lastOrNull { it !in confirmed && it != today } ?: today
            _state.update {
                it.copy(today = today, days = days, confirmedDays = confirmed, factors = repository.lifestyleFactors())
            }
            loadDay(start)
        }
    }

    fun onDaySelected(epochDay: Long) {
        viewModelScope.launch { loadDay(epochDay) }
    }

    private suspend fun loadDay(epochDay: Long) {
        val check = repository.load(epochDay) ?: DayCheckData(epochDay = epochDay, sleepHours = DEFAULT_SLEEP_HOURS)
        val from = clock.startOfDayMillis(epochDay)
        val to = clock.startOfDayMillis(epochDay + 1)
        val hasSymptoms = repository.symptomCount(from, to) > 0
        _state.update { it.copy(loading = false, check = check, hasSymptoms = hasSymptoms) }
    }

    fun onWellbeingChange(value: Int) = updateCheck { it.copy(wellbeing = value) }

    fun onSleepQualityChange(value: Int) = updateCheck { it.copy(sleepQuality = value) }

    fun onStressChange(value: Int) = updateCheck { it.copy(stress = value) }

    /** Sleep in half hours between 0 and 14 h. */
    fun onSleepHoursChange(delta: Float) = updateCheck {
        val hours = ((it.sleepHours ?: DEFAULT_SLEEP_HOURS) + delta).coerceIn(0f, MAX_SLEEP_HOURS)
        it.copy(sleepHours = hours)
    }

    fun onFactorToggle(factorId: Long) = updateCheck {
        it.copy(factorIds = if (factorId in it.factorIds) it.factorIds - factorId else it.factorIds + factorId)
    }

    /** Saves with the chosen status. NO_SYMPTOMS is refused when symptoms were logged that day. */
    fun onFinish(status: DayCheckStatus) {
        val current = _state.value
        if (status == DayCheckStatus.NO_SYMPTOMS && !current.canChooseNoSymptoms) return
        viewModelScope.launch {
            repository.save(current.check.copy(status = status))
            _state.update { it.copy(saved = true) }
        }
    }

    private fun updateCheck(change: (DayCheckData) -> DayCheckData) {
        _state.update { it.copy(check = change(it.check)) }
    }

    companion object {
        const val DAYS_BACK = 2
        const val DEFAULT_SLEEP_HOURS = 7f
        const val MAX_SLEEP_HOURS = 14f
        const val SLEEP_STEP_HOURS = 0.5f
    }
}
