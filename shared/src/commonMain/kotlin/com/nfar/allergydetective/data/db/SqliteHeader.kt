package com.nfar.allergydetective.data.db

/**
 * Every unencrypted SQLite file starts with this text. An encrypted (SQLCipher) file starts
 * with random bytes instead, so the header tells whether an old database still has to be
 * encrypted (feedback 20.11.).
 */
private val PLAIN_HEADER = "SQLite format 3\u0000".encodeToByteArray()

/** True when [firstBytes] (the start of a database file) is a plain, unencrypted SQLite file. */
fun isPlainSqlite(firstBytes: ByteArray): Boolean =
    firstBytes.size >= PLAIN_HEADER.size && PLAIN_HEADER.indices.all { firstBytes[it] == PLAIN_HEADER[it] }

/** Number of bytes [isPlainSqlite] needs. */
val SQLITE_HEADER_SIZE: Int get() = PLAIN_HEADER.size
