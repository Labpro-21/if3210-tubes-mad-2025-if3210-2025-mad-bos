package com.example.tubesmobdev.data.local.preferences


import kotlinx.coroutines.flow.Flow

interface IServicePreferences {

    val shouldRestartService: Flow<Boolean>

    suspend fun setShouldRestartService(value: Boolean)
}