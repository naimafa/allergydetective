package com.nfar.allergydetective.data.db

import androidx.room3.RoomDatabase
import kotlin.coroutines.CoroutineContext

/**
 * Finishes a platform-specific builder with the shared configuration.
 * The driver comes from the platform builder: SQLCipher on Android (encrypted, feedback 20.11.),
 * the bundled SQLite driver on iOS for now.
 */
fun createAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    ioContext: CoroutineContext,
): AppDatabase = builder
    .setQueryCoroutineContext(ioContext)
    .addMigrations(*Migrations.ALL)
    .build()
