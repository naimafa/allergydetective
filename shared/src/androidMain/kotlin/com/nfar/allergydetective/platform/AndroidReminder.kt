package com.nfar.allergydetective.platform

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.reminder_channel
import allergydetective.shared.generated.resources.reminder_text
import allergydetective.shared.generated.resources.reminder_title
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.domain.reminder.ReminderScheduler
import com.nfar.allergydetective.domain.reminder.ReminderSettings
import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.domain.reminder.delayUntilNext
import com.nfar.allergydetective.domain.time.AppClock
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Intent extra that opens the day check when the app is started from the reminder. */
const val EXTRA_OPEN_DAY_CHECK = "open_day_check"

class SharedPreferencesSettingsStore(context: Context) : SettingsStore {
    private val prefs = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    override fun reminder() = ReminderSettings(
        enabled = prefs.getBoolean(KEY_ENABLED, false),
        hour = prefs.getInt(KEY_HOUR, ReminderSettings.DEFAULT_HOUR),
        minute = prefs.getInt(KEY_MINUTE, 0),
    )

    override fun saveReminder(settings: ReminderSettings) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, settings.enabled)
            .putInt(KEY_HOUR, settings.hour)
            .putInt(KEY_MINUTE, settings.minute)
            .apply()
    }

    private companion object {
        const val KEY_ENABLED = "reminder_enabled"
        const val KEY_HOUR = "reminder_hour"
        const val KEY_MINUTE = "reminder_minute"
    }
}

/** Daily work at about the chosen time. WorkManager keeps it across reboots; a few minutes of delay are fine. */
class WorkManagerReminderScheduler(private val context: Context) : ReminderScheduler {

    override fun schedule(hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val delay = delayUntilNext(
            nowMinuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE),
            nowSecond = now.get(Calendar.SECOND),
            hour = hour,
            minute = minute,
        )
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "evening_reminder"
    }
}

/** Shows "Detective hour" unless today is already confirmed (UX 4: no extra pushes). */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    private val dayChecks: DayCheckRepository by inject()
    private val clock: AppClock by inject()

    override suspend fun doWork(): Result {
        val today = clock.today()
        if (today in dayChecks.confirmedDays(listOf(today))) return Result.success()
        if (!canNotify(applicationContext)) return Result.success()
        showNotification()
        return Result.success()
    }

    private suspend fun showNotification() {
        val context = applicationContext
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(Res.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT),
        )
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra(EXTRA_OPEN_DAY_CHECK, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = launch?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(getString(Res.string.reminder_title))
            .setContentText(getString(Res.string.reminder_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission") // checked in canNotify
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val CHANNEL_ID = "evening_reminder"
        const val NOTIFICATION_ID = 1
    }
}

fun canNotify(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
