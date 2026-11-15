package com.nfar.allergydetective.data.db

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.coroutines.CoroutineContext

/**
 * Finishes a platform-specific builder with the shared configuration.
 * The bundled SQLite driver gives identical behaviour on Android and iOS.
 */
fun createAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    ioContext: CoroutineContext,
): AppDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(ioContext)
    .build()
