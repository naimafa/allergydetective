package com.nfar.allergydetective.data.db

import android.util.Log
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.SupportSQLiteConnection
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File

/**
 * Room driver for the encrypted database on Android (feedback 20.11.), built on the official
 * SQLCipher library (net.zetetic:sqlcipher-android).
 *
 * SQLCipher's [SQLiteDatabase] is a SupportSQLiteDatabase, so androidx' [SupportSQLiteConnection]
 * turns it into a Room connection. There is deliberately no SupportSQLiteOpenHelper in between:
 * an open helper sets `user_version` itself and would get in the way of Room's own migrations.
 *
 * Like androidx' SupportSQLiteDriver, one database object is shared; it has its own pool and is
 * safe for concurrent use. A plain database from before this version is encrypted once on the
 * first open ([encryptPlainDatabase]).
 */
internal class SqlCipherDriver(
    private val path: String,
    private val key: DatabaseKey,
) : SQLiteDriver {

    private var database: SQLiteDatabase? = null

    @Suppress("INAPPLICABLE_JVM_NAME") // Due to KT-31420, as in androidx' SupportSQLiteDriver.
    @get:JvmName("hasConnectionPool")
    override val hasConnectionPool: Boolean
        get() = true

    @Synchronized
    override fun open(fileName: String): SQLiteConnection {
        require(fileName.substringAfterLast('/') == path.substringAfterLast('/')) {
            "This driver opens '$path' only, not '$fileName'."
        }
        val db = database?.takeIf { it.isOpen } ?: openEncrypted().also { database = it }
        return SupportSQLiteConnection(db)
    }

    private fun openEncrypted(): SQLiteDatabase {
        loadLibrary()
        val file = File(path)
        if (file.exists() && file.isPlain()) encryptPlainDatabase(file)
        return try {
            openWithKey(file)
        } catch (e: Exception) {
            if (!file.exists()) throw e
            // Only when the key is gone (e.g. the Keystore was reset) the file cannot be read any more.
            // Without the key nobody can decrypt it, so start empty instead of failing on every start.
            Log.e(TAG, "Encrypted database cannot be opened, starting with an empty one", e)
            deleteDatabaseFiles(file)
            key.reset()
            openWithKey(file)
        }
    }

    private fun openWithKey(file: File): SQLiteDatabase = SQLiteDatabase.openDatabase(
        file.absolutePath,
        key.passphrase().toByteArray(Charsets.UTF_8),
        null,
        SQLiteDatabase.CREATE_IF_NECESSARY or SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING,
        null,
        null,
    )

    /**
     * Copies the plain database into an encrypted one with `sqlcipher_export` and replaces it.
     * The plain file is deleted only after the encrypted copy was opened and checked, so a failure
     * leaves the old data untouched (the next start tries again).
     */
    private fun encryptPlainDatabase(plain: File) {
        val passphrase = key.passphrase()
        val target = File(plain.path + ENCRYPTING_SUFFIX)
        deleteDatabaseFiles(target)
        val version: Int
        // An empty key opens a plain file with SQLCipher.
        SQLiteDatabase.openDatabase(plain.absolutePath, "", null, SQLiteDatabase.OPEN_READWRITE, null, null).use { db ->
            // Bring the write-ahead log into the main file first, so nothing is left behind.
            db.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
            version = db.version
            db.execSQL("ATTACH DATABASE ? AS encrypted KEY ?", arrayOf<Any>(target.absolutePath, passphrase))
            db.query("SELECT sqlcipher_export('encrypted')").use { it.moveToFirst() }
            db.execSQL("PRAGMA encrypted.user_version = $version")
            db.execSQL("DETACH DATABASE encrypted")
        }
        SQLiteDatabase.openDatabase(target.absolutePath, passphrase.toByteArray(Charsets.UTF_8), null, SQLiteDatabase.OPEN_READWRITE, null, null)
            .use { db -> check(db.version == version) { "Encrypted copy has version ${db.version}, expected $version" } }
        deleteDatabaseFiles(plain)
        check(target.renameTo(plain)) { "Could not replace the plain database" }
        Log.i(TAG, "Database encrypted (version $version)")
    }

    private fun File.isPlain(): Boolean {
        val header = ByteArray(SQLITE_HEADER_SIZE)
        val read = inputStream().use { it.read(header) }
        return read == header.size && isPlainSqlite(header)
    }

    private fun deleteDatabaseFiles(file: File) {
        listOf("", "-wal", "-shm", "-journal").forEach { File(file.path + it).delete() }
    }

    private companion object {
        const val TAG = "SqlCipherDriver"
        const val ENCRYPTING_SUFFIX = ".encrypting"

        @Volatile
        private var loaded = false

        fun loadLibrary() {
            if (!loaded) {
                System.loadLibrary("sqlcipher")
                loaded = true
            }
        }
    }
}
