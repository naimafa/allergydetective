package com.nfar.allergydetective.data.db

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * The passphrase of the encrypted database (feedback 20.11.).
 *
 * A random 256-bit value, created once. It is stored only wrapped (AES-GCM) with a key that
 * lives in the Android Keystore and never leaves it, so a copy of the app files alone does not
 * open the database. The user does not need a password. Backup is off (TRD 3.5), so the
 * wrapped value never leaves the device either.
 */
internal class DatabaseKey(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** True once a passphrase was created: the database on disk is (or will be) encrypted with it. */
    fun exists(): Boolean = prefs.contains(KEY_WRAPPED)

    /** The passphrase as hex text (64 characters); created on first use. */
    @Synchronized
    fun passphrase(): String {
        prefs.getString(KEY_WRAPPED, null)?.let { return unwrap(it) }
        val raw = ByteArray(KEY_BYTES).also(SecureRandom()::nextBytes)
        val hex = raw.toHex()
        raw.fill(0)
        // commit, not apply: the key must be on disk before data is encrypted with it.
        check(prefs.edit().putString(KEY_WRAPPED, wrap(hex)).commit()) { "Could not store the database key" }
        return hex
    }

    /** Forgets the passphrase; only after the encrypted database was found unreadable and deleted. */
    @Synchronized
    fun reset() {
        prefs.edit().remove(KEY_WRAPPED).commit()
        runCatching { KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }.deleteEntry(ALIAS) }
    }

    private fun keystoreKey(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (store.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private fun wrap(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, keystoreKey()) }
        val sealed = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + sealed, Base64.NO_WRAP)
    }

    private fun unwrap(stored: String): String {
        val bytes = Base64.decode(stored, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, IV_BYTES)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, keystoreKey(), GCMParameterSpec(TAG_BITS, iv))
        }
        return String(cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES), Charsets.UTF_8)
    }

    private fun ByteArray.toHex(): String = joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

    private companion object {
        const val PREFS = "database_key"
        const val KEY_WRAPPED = "wrapped_passphrase"
        const val ALIAS = "allergy_detective_db"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BYTES = 32
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
