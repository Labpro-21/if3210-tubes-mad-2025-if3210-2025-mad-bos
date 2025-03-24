package com.example.tubesmobdev.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tubesmobdev.util.EncryptionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthPreferences(private val context: Context): IAuthPreferences {
    private val Context.dataStore by preferencesDataStore(name = "auth_datastore")
    private val encryptionManager = EncryptionManager(context)

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    override suspend fun saveAccessToken(token: String) {
        val encrypted = encryptionManager.encrypt(token)
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = encrypted
        }
    }

    override suspend fun getToken(): String? {
        val encrypted = context.dataStore.data.map { it[TOKEN_KEY] }.first()
        return encrypted?.let { encryptionManager.decrypt(it) }
    }

    override suspend fun saveRefreshToken(refreshToken: String) {
        val encrypted = encryptionManager.encrypt(refreshToken)
        context.dataStore.edit { prefs ->
            prefs[REFRESH_TOKEN_KEY] = encrypted
        }
    }

    override suspend fun getRefreshToken(): String? {
        val encrypted = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }.first()
        return encrypted?.let { encryptionManager.decrypt(it) }
    }

    override suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}