package com.example.tubesmobdev.data.local.preferences

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tubesmobdev.util.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject

class AuthPreferences @Inject constructor(@ApplicationContext private val context: Context): IAuthPreferences {
    private val Context.dataStore by preferencesDataStore(name = "auth_datastore")
    private val encryptionManager = EncryptionManager(context)

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    override suspend fun saveAccessToken(token: String) {
        val encrypted = encryptionManager.encrypt(token)
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = encrypted
        }
        setUserIdFromToken(token)
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
            prefs.remove(USER_ID_KEY)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    override val isLoggedInFlow: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[TOKEN_KEY]?.let { encryptionManager.decrypt(it) } != null
        }

    private suspend fun setUserIdFromToken(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payloadEncoded = parts[1]
                val decodedBytes = Base64.decode(payloadEncoded, Base64.URL_SAFE or Base64.NO_WRAP)
                val payloadJson = String(decodedBytes, Charsets.UTF_8)
                val jsonObject = JSONObject(payloadJson)
                val userId = jsonObject.optString("id")
                if (userId != "") {
                    context.dataStore.edit { prefs ->
                        prefs[USER_ID_KEY] = userId
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getUserId(): Long? {
        return context.dataStore.data.map { it[USER_ID_KEY] }.first()?.toLong()
    }
}
