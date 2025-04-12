package com.example.tubesmobdev.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tubesmobdev.data.model.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "player_preferences")

@Singleton
class PlayerPreferences @Inject constructor(
    private val context: Context
) : IPlayerPreferences {

    companion object {
        private val KEY_QUEUE = stringPreferencesKey("current_queue")
    }

    override suspend fun saveQueue(queue: List<Song>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_QUEUE] = Gson().toJson(queue)
        }
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