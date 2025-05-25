package com.example.tubesmobdev.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tubesmobdev.data.remote.response.ProfileResponse
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.profileDataStore by preferencesDataStore(name = "profile_pref")

@Singleton
class ProfilePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : IProfilePreferences {

    private val PROFILE_KEY = stringPreferencesKey("cached_profile")

    override suspend fun saveProfile(profile: ProfileResponse) {
        val json = Gson().toJson(profile)
        context.profileDataStore.edit { preferences ->
            preferences[PROFILE_KEY] = json
        }
    }

    override suspend fun getCachedProfile(): ProfileResponse? {
        val preferences = context.profileDataStore.data.first()
        val json = preferences[PROFILE_KEY]
        return json?.let {
            Gson().fromJson(it, ProfileResponse::class.java)
        }
    }

    override suspend fun clearProfile() {
        context.profileDataStore.edit { preferences ->
            preferences.remove(PROFILE_KEY)
        }
    }
}