package com.nfar.allergydetective.data.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

// Not built yet (no Mac). iCloud backup exclusion follows with the BackupPolicy interface.
@OptIn(ExperimentalForeignApi::class)
fun appDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val documentDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dbPath = requireNotNull(documentDir?.path) + "/" + DATABASE_NAME
    return Room.databaseBuilder<AppDatabase>(name = dbPath)
}
