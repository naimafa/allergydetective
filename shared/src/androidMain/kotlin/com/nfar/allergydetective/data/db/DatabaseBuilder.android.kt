package com.nfar.allergydetective.data.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

fun appDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
