package com.example.tubesmobdev.util

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager

class EncryptionManager(context: Context) {
    companion object {
        private const val KEYSET_NAME = "auth_keyset"
        private const val PREF_NAME = "auth_encrypted_prefs"
        private const val MASTER_KEY_URI = "android-keystore://auth_master_key"
    }

    private val aead: Aead

    init {
        AeadConfig.register()

        val keysetHandle: KeysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_NAME)
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle

        aead = keysetHandle.getPrimitive(Aead::class.java)
    }

    fun encrypt(plainText: String): String {
        val cipherText = aead.encrypt(plainText.toByteArray(), null)
        return Base64.encodeToString(cipherText, Base64.DEFAULT)
    }

    fun decrypt(cipherText: String): String {
        val decrypted = aead.decrypt(Base64.decode(cipherText, Base64.DEFAULT), null)
        return String(decrypted)
    }
}