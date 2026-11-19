package com.nfar.allergydetective.ui.theme

import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.domain.reminder.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The chosen look (feedback 19.11.: dark mode for the night), read by the app root and changed in the profile. */
class ThemeController(private val store: SettingsStore) {
    private val _mode = MutableStateFlow(store.themeMode())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun onModeChange(mode: ThemeMode) {
        store.saveThemeMode(mode)
        _mode.value = mode
    }
}
