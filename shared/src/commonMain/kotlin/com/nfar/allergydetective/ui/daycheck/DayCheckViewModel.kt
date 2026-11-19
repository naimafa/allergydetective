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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class DayCheckState(
    val loading: Boolean = true,
    val today: Long = 0,
    /** Today and up to two days back (UX 3.4), newest first. */
    val days: List<Long> = emptyList(),
    val confirmedDays: Set<Long> = emptySet(),
    val check: DayCheckData = DayCheckData(epochDay = 0),
    val factors: List<FactorInfo> = emptyList(),
    /** Symptoms were logged on this day: finishing confirms "all logged" without asking. */
    val hasSymptoms: Boolean = false,
    /** No symptoms logged: "Finish day" asks "no complaints on this day?" first. */
    val askNoSymptoms: Boolean = false,
    /** Set after finishing, the screen closes then. */
    val saved: Boolean = false,
) {
    /** Past days in the selection that are not confirmed yet, newest first. */
    val openPastDays: List<Long> get() = days.filter { it != today && it !in confirmedDays }
}

/**
 * Day check (UX 3.4): wellbeing, sleep, stress, factors and the confirmation.
 * Self-test 19.11.: every answer is saved right away (the day stays open), one button "Finish day"
 * confirms it; the three status buttons with the unclear "add later" are gone.
 */
class DayCheckViewModel(
    private val repository: DayCheckRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(DayCheckState())

    /** Saves one after the other, so a quick second tap never overtakes the first. */
    private val saveLock = Mutex()
    val state: StateFlow<DayCheckState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val today = clock.today()
            val days = (0..DAYS_BACK).map { today - it }
            val confirmed = repository.confirmedDays(days)
            // Always start with today: preselecting an open past day made users confirm the wrong day.
            // Open past days are only pointed out (openPastDays) and chosen by tapping.
            _state.update {
                it.copy(today = today, days = days, confirmedDays = confirmed, factors = repository.lifestyleFactors())
            }
            loadDay(today)
        }
    }

    fun onDaySelected(epochDay: Long) {
        viewModelScope.launch { loadDay(epochDay) }
    }

    /** Back on the screen, e.g. after logging a symptom from the "no complaints?" question. */
    fun onResume() {
        if (_state.value.loading) return
        viewModelScope.launch { _state.update { it.copy(hasSymptoms = hasSymptoms(it.check.epochDay)) } }
    }

    private suspend fun loadDay(epochDay: Long) {
        val check = repository.load(epochDay) ?: DayCheckData(epochDay = epochDay, sleepHours = DEFAULT_SLEEP_HOURS)
        _state.update { it.copy(loading = false, check = check, hasSymptoms = hasSymptoms(epochDay), askNoSymptoms = false) }
    }

    private suspend fun hasSymptoms(epochDay: Long): Boolean =
        repository.symptomCount(clock.startOfDayMillis(epochDay), clock.startOfDayMillis(epochDay + 1)) > 0

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

    /** "Finish day": with symptoms the day is complete; without, the screen asks first. */
    fun onFinish() {
        if (_state.value.hasSymptoms) finish(DayCheckStatus.COMPLETE) else _state.update { it.copy(askNoSymptoms = true) }
    }

    /** Answer "yes, no complaints". */
    fun onConfirmNoSymptoms() = finish(DayCheckStatus.NO_SYMPTOMS)

    /** "Not yet" or "log a symptom first": the day stays open. */
    fun onNoSymptomsDismissed() {
        _state.update { it.copy(askNoSymptoms = false) }
    }

    private fun finish(status: DayCheckStatus) {
        val check = _state.value.check.copy(status = status)
        _state.update { it.copy(check = check, askNoSymptoms = false) }
        viewModelScope.launch {
            saveLock.withLock { repository.save(check) }
            _state.update { it.copy(saved = true) }
        }
    }

    /** Applies the change and saves it at once; the confirmation status stays as it is. */
    private fun updateCheck(change: (DayCheckData) -> DayCheckData) {
        val check = change(_state.value.check)
        _state.update { it.copy(check = check) }
        viewModelScope.launch { saveLock.withLock { repository.save(check) } }
    }

    companion object {
        const val DAYS_BACK = 2
        const val DEFAULT_SLEEP_HOURS = 7f
        const val MAX_SLEEP_HOURS = 14f
        const val SLEEP_STEP_HOURS = 0.5f
    }
}
