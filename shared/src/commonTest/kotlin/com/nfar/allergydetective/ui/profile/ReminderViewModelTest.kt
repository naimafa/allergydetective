package com.nfar.allergydetective.ui.profile

import com.nfar.allergydetective.domain.reminder.ReminderScheduler
import com.nfar.allergydetective.domain.reminder.ReminderSettings
import com.nfar.allergydetective.domain.reminder.SettingsStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReminderViewModelTest {

    private val store = object : SettingsStore {
        var settings = ReminderSettings()
        override fun reminder() = settings
        override fun saveReminder(settings: ReminderSettings) {
            this.settings = settings
        }
    }

    private val calls = mutableListOf<String>()
    private val scheduler = object : ReminderScheduler {
        override fun schedule(hour: Int, minute: Int) {
            calls += "schedule $hour:$minute"
        }
        override fun cancel() {
            calls += "cancel"
        }
    }

    @Test
    fun reminderIsOffByDefault() {
        assertFalse(ReminderViewModel(store, scheduler).state.value.settings.enabled)
    }

    @Test
    fun enablingStoresAndSchedules() {
        val vm = ReminderViewModel(store, scheduler)

        vm.onEnabledChange(true)
        vm.onTimeChange(21, 0)

        assertEquals(ReminderSettings(enabled = true, hour = 21, minute = 0), store.settings)
        assertEquals(listOf("schedule 20:0", "schedule 21:0"), calls)
    }

    @Test
    fun deniedPermissionKeepsTheReminderOff() {
        val vm = ReminderViewModel(store, scheduler)

        vm.onEnabledChange(true, permissionGranted = false)

        assertTrue(vm.state.value.permissionDenied)
        assertFalse(store.settings.enabled)
        assertTrue(calls.isEmpty())
    }

    @Test
    fun disablingCancels() {
        store.settings = ReminderSettings(enabled = true)
        val vm = ReminderViewModel(store, scheduler)

        vm.onEnabledChange(false)

        assertEquals(listOf("cancel"), calls)
    }
}
