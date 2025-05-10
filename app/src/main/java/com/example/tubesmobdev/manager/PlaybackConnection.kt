package com.example.tubesmobdev.manager

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.work.await
import com.example.tubesmobdev.service.MusicService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


@UnstableApi
class PlaybackConnection @Inject constructor(
    @ApplicationContext context: Context
) {
    private val controllerFuture =
        MediaController.Builder(context, SessionToken(context, ComponentName(context, MusicService::class.java)))
            .buildAsync()

    @SuppressLint("RestrictedApi")
    suspend fun getController(): MediaController {
        return controllerFuture.await()
    }
}
