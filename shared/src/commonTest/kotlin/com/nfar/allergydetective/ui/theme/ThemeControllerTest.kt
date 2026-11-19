package com.nfar.allergydetective.ui.theme

import com.nfar.allergydetective.domain.reminder.ReminderSettings
import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.domain.reminder.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeControllerTest {

    private class FakeStore(var saved: ThemeMode = ThemeMode.SYSTEM) : SettingsStore {
        override fun reminder() = ReminderSettings()
        override fun saveReminder(settings: ReminderSettings) = Unit
        override fun themeMode() = saved
        override fun saveThemeMode(mode: ThemeMode) {
            saved = mode
        }
    }

    @Test
    fun storedModeIsUsedAndAChangeIsSaved() {
        val store = FakeStore(ThemeMode.LIGHT)
        val controller = ThemeController(store)
        assertEquals(ThemeMode.LIGHT, controller.mode.value)

        controller.onModeChange(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, controller.mode.value)
        assertEquals(ThemeMode.DARK, store.saved)
    }

    @Test
    fun withoutAChoiceTheAppFollowsTheSystem() {
        val store = object : SettingsStore {
            override fun reminder() = ReminderSettings()
            override fun saveReminder(settings: ReminderSettings) = Unit
        }

        assertEquals(ThemeMode.SYSTEM, ThemeController(store).mode.value)
    }
}
