package com.nfar.allergydetective.data.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

/** Android: the database is encrypted with SQLCipher, the key is protected by the Keystore (feedback 20.11.). */
fun appDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    ).setDriver(SqlCipherDriver(dbFile.absolutePath, DatabaseKey(appContext)))
}
