package com.nfar.allergydetective.di

import android.content.Context
import androidx.room3.RoomDatabase
import com.nfar.allergydetective.AppStartup
import com.nfar.allergydetective.data.db.AppDatabase
import com.nfar.allergydetective.data.db.appDatabaseBuilder
import com.nfar.allergydetective.domain.reminder.ReminderScheduler
import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.platform.SharedPreferencesSettingsStore
import com.nfar.allergydetective.platform.WorkManagerReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<RoomDatabase.Builder<AppDatabase>> { appDatabaseBuilder(androidContext()) }
    single<SettingsStore> { SharedPreferencesSettingsStore(androidContext()) }
    single<ReminderScheduler> { WorkManagerReminderScheduler(androidContext()) }
}

/** Entry point for the Android app: call once from `Application.onCreate`. */
fun startApp(context: Context) {
    val koin = initKoin { androidContext(context.applicationContext) }.koin
    koin.get<AppStartup>().start()
}
