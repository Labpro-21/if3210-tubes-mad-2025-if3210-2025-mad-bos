package com.example.tubesmobdev.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ServicePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : IServicePreferences {
    private val dataStore = context.dataStore

    override val shouldRestartService: Flow<Boolean> = dataStore.data.map {
        it[booleanPreferencesKey("should_restart_service")] ?: true
    }

    override suspend fun setShouldRestartService(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("should_restart_service")] = value
        }
    }
}
