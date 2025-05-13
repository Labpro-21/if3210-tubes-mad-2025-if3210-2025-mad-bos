package com.example.tubesmobdev.util

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.io.File

class EncryptionManager(context: Context) {
    companion object {
        private const val KEYSET_NAME = "auth_keyset"
        private const val PREF_NAME = "auth_encrypted_prefs"
        private const val MASTER_KEY_URI = "android-keystore://auth_master_key"
        private const val TAG = "EncryptionManager"
    }

    private val aead: Aead

    init {
        AeadConfig.register()

        val keysetHandle = try {
            AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, PREF_NAME)
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .keysetHandle
        } catch (e: Exception) {
            Log.e(TAG, "Keyset corrupt or unreadable, attempting reset", e)

            // 🔥 Hapus shared preferences yang menyimpan keyset
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()

            // 💡 Coba generate ulang keyset
            AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, PREF_NAME)
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .keysetHandle
        }

        aead = keysetHandle.getPrimitive(Aead::class.java)
    }

    fun encrypt(plainText: String): String {
        return try {
            val cipherText = aead.encrypt(plainText.toByteArray(), null)
            Base64.encodeToString(cipherText, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Encrypt error", e)
            ""
        }
    }

    fun decrypt(cipherText: String): String {
        return try {
            val decrypted = aead.decrypt(Base64.decode(cipherText, Base64.DEFAULT), null)
            String(decrypted)
        } catch (e: Exception) {
            Log.e(TAG, "Decrypt error", e)
            ""
        }
    }
}