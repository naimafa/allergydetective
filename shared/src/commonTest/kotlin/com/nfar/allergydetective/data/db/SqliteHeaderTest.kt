package com.nfar.allergydetective.data.db

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The header decides whether an old database is encrypted once on the first start (feedback 20.11.). */
class SqliteHeaderTest {

    @Test
    fun plainSqliteFileIsRecognised() {
        val header = "SQLite format 3\u0000".encodeToByteArray() + ByteArray(84)
        assertTrue(isPlainSqlite(header))
    }

    @Test
    fun encryptedOrShortFilesAreNotPlain() {
        // SQLCipher files start with the random salt.
        assertFalse(isPlainSqlite(byteArrayOf(0x3A, 0x7F, -0x10, 0x01) + ByteArray(96)))
        assertFalse(isPlainSqlite("SQLite format".encodeToByteArray()))
        assertFalse(isPlainSqlite(ByteArray(0)))
    }
}
