package com.example.tubesmobdev.di

import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

//tidak digunakan
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun workerFactory(): HiltWorkerFactory
}
