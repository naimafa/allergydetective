package com.nfar.allergydetective

import com.nfar.allergydetective.data.seed.DatabaseSeeder
import com.nfar.allergydetective.domain.reminder.ReminderScheduler
import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.domain.reminder.apply
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.nfar.allergydetective.data.diary.DuplicateIngredientMerger

/** Work that runs once per process start, off the main thread. */
class AppStartup(
    private val seeder: DatabaseSeeder,
    private val scope: CoroutineScope,
    private val settings: SettingsStore,
    private val reminders: ReminderScheduler,
    private val duplicates: DuplicateIngredientMerger,
) {
    /**
     * Fills the reference tables on the very first start and re-applies the reminder setting
     * (keeps the schedule in sync after updates). Custom ingredients that duplicate a seed
     * ingredient are merged into it. Returns the job so callers can wait for it.
     */
    fun start(): Job = scope.launch {
        seeder.seedIfEmpty()
        duplicates.mergeIntoSeed()
        reminders.apply(settings.reminder())
    }
}
