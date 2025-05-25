package com.example.tubesmobdev.data.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tubesmobdev.data.remote.response.OnlineSong
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

    private val gson = Gson()

    private fun globalKey(): Preferences.Key<String> = stringPreferencesKey("top_global")
    private fun countryKey(code: String): Preferences.Key<String> = stringPreferencesKey("top_country_$code")

    override suspend fun saveTopGlobalSongs(songs: List<OnlineSong>) {
        val json = gson.toJson(songs)
        context.songDataStore.edit { it[globalKey()] = json }
    }

    override suspend fun getTopGlobalSongs(): List<OnlineSong>? {
        val json = context.songDataStore.data.first()[globalKey()] ?: return null
        Log.d("debug", "getTopGlobalSongs: "+json)
        return try {
            val type = object : TypeToken<List<OnlineSong>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveTopSongsByCountry(countryCode: String, songs: List<OnlineSong>) {
        val json = gson.toJson(songs)
        context.songDataStore.edit { it[countryKey(countryCode)] = json }
    }

    override suspend fun getTopSongsByCountry(countryCode: String): List<OnlineSong>? {
        val json = context.songDataStore.data.first()[countryKey(countryCode)] ?: return null
        Log.d("debug", "getTopSongsByCountry: "+json)
        return try {
            val type = object : TypeToken<List<OnlineSong>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
}