package com.nfar.allergydetective.ui.profile

import androidx.lifecycle.ViewModel
import com.nfar.allergydetective.domain.reminder.ReminderScheduler
import com.nfar.allergydetective.domain.reminder.ReminderSettings
import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.domain.reminder.apply
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReminderState(
    val settings: ReminderSettings,
    /** The system refused notifications, the switch stays off. */
    val permissionDenied: Boolean = false,
)

/** Reminder settings in the profile (UX 4). */
class ReminderViewModel(
    private val store: SettingsStore,
    private val scheduler: ReminderScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderState(store.reminder()))
    val state: StateFlow<ReminderState> = _state.asStateFlow()

    /** Called after the notification permission was asked (or was not needed). */
    fun onEnabledChange(enabled: Boolean, permissionGranted: Boolean = true) {
        if (enabled && !permissionGranted) {
            _state.value = _state.value.copy(permissionDenied = true)
            return
        }
        update(_state.value.settings.copy(enabled = enabled))
    }

    fun onTimeChange(hour: Int, minute: Int) {
        update(_state.value.settings.copy(hour = hour, minute = minute))
    }

    private fun update(settings: ReminderSettings) {
        store.saveReminder(settings)
        scheduler.apply(settings)
        _state.value = ReminderState(settings)
    }

    companion object {
        /** Time chips of the profile; other times follow with the design phase. */
        val HOURS = listOf(19, 20, 21, 22)
    }
}
