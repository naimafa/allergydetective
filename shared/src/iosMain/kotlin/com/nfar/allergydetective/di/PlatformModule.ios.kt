package com.nfar.allergydetective.di

import androidx.room3.RoomDatabase
import com.nfar.allergydetective.AppStartup
import com.nfar.allergydetective.data.db.AppDatabase
import com.nfar.allergydetective.data.db.appDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

// Not built yet (no Mac).
actual val platformModule: Module = module {
    single<RoomDatabase.Builder<AppDatabase>> { appDatabaseBuilder() }
}

/** Entry point for the iOS app: call once from the `init` of `iOSApp`. */
fun startApp() {
    val koin = initKoin().koin
    koin.get<AppStartup>().start()
}
