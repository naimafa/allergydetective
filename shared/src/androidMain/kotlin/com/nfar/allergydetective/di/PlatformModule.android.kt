package com.nfar.allergydetective.di

import android.content.Context
import androidx.room3.RoomDatabase
import com.nfar.allergydetective.AppStartup
import com.nfar.allergydetective.data.db.AppDatabase
import com.nfar.allergydetective.data.db.appDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<RoomDatabase.Builder<AppDatabase>> { appDatabaseBuilder(androidContext()) }
}

/** Entry point for the Android app: call once from `Application.onCreate`. */
fun startApp(context: Context) {
    val koin = initKoin { androidContext(context.applicationContext) }.koin
    koin.get<AppStartup>().start()
}
