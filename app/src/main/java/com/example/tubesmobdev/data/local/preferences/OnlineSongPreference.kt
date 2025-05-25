package com.example.tubesmobdev.data.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tubesmobdev.data.remote.response.OnlineSong
import com.example.tubesmobdev.util.EncryptionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.songDataStore by preferencesDataStore("online_song_cache")

@Singleton
class OnlineSongPreference @Inject constructor(
    @ApplicationContext private val context: Context
) : IOnlineSongPreference {

    private val encryptionManager = EncryptionManager(context)
    private val gson = Gson()

    private fun globalKey(): Preferences.Key<String> = stringPreferencesKey("top_global")
    private fun countryKey(code: String): Preferences.Key<String> = stringPreferencesKey("top_country_$code")

    override suspend fun saveTopGlobalSongs(songs: List<OnlineSong>) {
        val json = gson.toJson(songs)
        val encrypted = encryptionManager.encrypt(json)
        context.songDataStore.edit { it[globalKey()] = encrypted }
    }

    override suspend fun getTopGlobalSongs(): List<OnlineSong>? {
        val encrypted = context.songDataStore.data.first()[globalKey()] ?: return null
        return try {
            val decrypted = encryptionManager.decrypt(encrypted)
            val type = object : TypeToken<List<OnlineSong>>() {}.type
            gson.fromJson(decrypted, type)
        } catch (e: Exception) {
            Log.e("OnlineSongPreference", "Failed to decrypt or parse global songs", e)
            null
        }
    }

    override suspend fun saveTopSongsByCountry(countryCode: String, songs: List<OnlineSong>) {
        val json = gson.toJson(songs)
        val encrypted = encryptionManager.encrypt(json)
        context.songDataStore.edit { it[countryKey(countryCode)] = encrypted }
    }

    override suspend fun getTopSongsByCountry(countryCode: String): List<OnlineSong>? {
        val encrypted = context.songDataStore.data.first()[countryKey(countryCode)] ?: return null
        return try {
            val decrypted = encryptionManager.decrypt(encrypted)
            val type = object : TypeToken<List<OnlineSong>>() {}.type
            gson.fromJson(decrypted, type)
        } catch (e: Exception) {
            Log.e("OnlineSongPreference", "Failed to decrypt or parse songs for $countryCode", e)
            null
        }
    }
}