package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable
import com.nfar.allergydetective.domain.reminder.ReminderScheduler
import com.nfar.allergydetective.domain.reminder.ReminderSettings
import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.domain.reminder.ThemeMode
import platform.Foundation.NSUserDefaults

// Not built yet (no Mac). Local notifications (UNUserNotificationCenter) follow with the iOS build.

class UserDefaultsSettingsStore : SettingsStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun reminder(): ReminderSettings {
        val hasHour = defaults.objectForKey(KEY_HOUR) != null
        return ReminderSettings(
            enabled = defaults.boolForKey(KEY_ENABLED),
            hour = if (hasHour) defaults.integerForKey(KEY_HOUR).toInt() else ReminderSettings.DEFAULT_HOUR,
            minute = defaults.integerForKey(KEY_MINUTE).toInt(),
        )
    }

    override fun saveReminder(settings: ReminderSettings) {
        defaults.setBool(settings.enabled, KEY_ENABLED)
        defaults.setInteger(settings.hour.toLong(), KEY_HOUR)
        defaults.setInteger(settings.minute.toLong(), KEY_MINUTE)
    }

    override fun themeMode(): ThemeMode =
        defaults.stringForKey(KEY_THEME)?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: ThemeMode.AUTO

    override fun saveThemeMode(mode: ThemeMode) {
        defaults.setObject(mode.name, KEY_THEME)
    }

    override fun symptomGroupOrder(): List<String> =
        defaults.stringForKey(KEY_SYMPTOM_GROUPS)?.split(',')?.filter { it.isNotBlank() }.orEmpty()

    override fun saveSymptomGroupOrder(order: List<String>) {
        defaults.setObject(order.joinToString(","), KEY_SYMPTOM_GROUPS)
    }

    private companion object {
        const val KEY_SYMPTOM_GROUPS = "symptom_group_order"
        const val KEY_THEME = "theme_mode"
        const val KEY_ENABLED = "reminder_enabled"
        const val KEY_HOUR = "reminder_hour"
        const val KEY_MINUTE = "reminder_minute"
    }
}

class NoOpReminderScheduler : ReminderScheduler {
    override fun schedule(hour: Int, minute: Int) = Unit
    override fun cancel() = Unit
}

@Composable
actual fun rememberNotificationPermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit = { onResult(true) }
