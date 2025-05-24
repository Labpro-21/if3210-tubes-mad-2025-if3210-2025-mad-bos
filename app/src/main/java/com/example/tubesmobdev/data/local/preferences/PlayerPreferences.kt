package com.example.tubesmobdev.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tubesmobdev.data.model.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "player_preferences")

@Singleton
class PlayerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : IPlayerPreferences {

    companion object {
        private val KEY_QUEUE = stringPreferencesKey("current_queue")
        private val KEY_LAST_QUEUE = stringPreferencesKey("last_queue")
        private val KEY_LAST_PLAYED_SONG = stringPreferencesKey("last_played_song")
        private val KEY_LAST_POSITION = stringPreferencesKey("last_position")
    }

    override suspend fun saveLastQueue(queue: List<Song>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_QUEUE] = Gson().toJson(queue)
        }
    }


    override suspend fun saveLastPosition(position: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_POSITION] = position.toString()
        }
    }

    override suspend fun clearLastSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_LAST_QUEUE)
            prefs.remove(KEY_LAST_PLAYED_SONG)
            prefs.remove(KEY_LAST_POSITION)
        }
    }

    override suspend fun saveLastPlayedSong(song: Song) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_PLAYED_SONG] = Gson().toJson(song)
        }
    }

    override val getLastPosition: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_POSITION]?.toLongOrNull() ?: 0L
    }

    override suspend fun saveQueue(queue: List<Song>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_QUEUE] = Gson().toJson(queue)
        }
    }

    override val getLastQueue: Flow<List<Song>> = context.dataStore.data.map { preferences ->
        val json = preferences[KEY_LAST_QUEUE]
        if (json.isNullOrEmpty()) emptyList()
        else Gson().fromJson(json, object : TypeToken<List<Song>>() {}.type)
    }

    override val getLastPlayedSong: Flow<Song?> = context.dataStore.data.map { preferences ->
        val json = preferences[KEY_LAST_PLAYED_SONG]
        json?.let { Gson().fromJson(it, Song::class.java) }
    }


    override val getQueue: Flow<List<Song>> = context.dataStore.data.map { preferences ->
        val json = preferences[KEY_QUEUE]
        if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            Gson().fromJson(json, object : TypeToken<List<Song>>() {}.type)
        }
    }

    override suspend fun clearQueue() {
        context.dataStore.edit { it.remove(KEY_QUEUE) }
    }
}