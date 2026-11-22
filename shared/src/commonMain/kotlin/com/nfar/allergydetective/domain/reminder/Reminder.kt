package com.nfar.allergydetective.domain.reminder

/** Evening reminder for the day check (UX 4). Off until the user turns it on. */
data class ReminderSettings(
    val enabled: Boolean = false,
    val hour: Int = DEFAULT_HOUR,
    val minute: Int = 0,
) {
    companion object {
        const val DEFAULT_HOUR = 20
    }
}

/** Small key-value storage for settings. Android: SharedPreferences, iOS: NSUserDefaults. */
interface SettingsStore {
    fun reminder(): ReminderSettings
    fun saveReminder(settings: ReminderSettings)

    /** Automatic, light or dark (feedback 19.11.). */
    fun themeMode(): ThemeMode = ThemeMode.AUTO
    fun saveThemeMode(mode: ThemeMode) = Unit

    /** App language (user note 21.11.2026): null = like the phone, otherwise "de", "en", "fr" or "ar". */
    fun appLanguage(): String? = null
    fun saveAppLanguage(language: String?) = Unit
}

/**
 * App look (feedback 19.11.): AUTO is dark in the evening and at night (20–7 h) and whenever the
 * phone itself is dark, light otherwise; or always light / dark.
 */
enum class ThemeMode {
    AUTO,
    LIGHT,
    DARK,
    ;

    companion object {
        const val NIGHT_START_HOUR = 20
        const val NIGHT_END_HOUR = 7

        fun isNight(hour: Int): Boolean = hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR
    }
}

/** Platform adapter (ARCHITEKTUR): schedules the daily notification. */
interface ReminderScheduler {
    fun schedule(hour: Int, minute: Int)
    fun cancel()
}

/** Applies the stored settings, e.g. at app start or after a change. */
fun ReminderScheduler.apply(settings: ReminderSettings) {
    if (settings.enabled) schedule(settings.hour, settings.minute) else cancel()
}

/**
 * Milliseconds from [nowMinuteOfDay] (local minutes since midnight) until the next [hour]:[minute].
 * A reminder time that has just passed today moves to tomorrow.
 */
fun delayUntilNext(nowMinuteOfDay: Int, nowSecond: Int, hour: Int, minute: Int): Long {
    val target = hour * 60 + minute
    var minutes = target - nowMinuteOfDay
    if (minutes <= 0) minutes += MINUTES_PER_DAY
    return minutes * 60_000L - nowSecond * 1_000L
}

private const val MINUTES_PER_DAY = 24 * 60
