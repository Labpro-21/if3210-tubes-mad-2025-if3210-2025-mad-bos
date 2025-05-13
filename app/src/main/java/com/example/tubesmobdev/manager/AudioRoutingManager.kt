package com.example.tubesmobdev.manager

import android.content.Context
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter.RouteInfo
import com.example.tubesmobdev.domain.model.AudioOutputDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRoutingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val mediaRouter: MediaRouter = MediaRouter.getInstance(context)

    fun getAvailableRoutes(): List<AudioOutputDevice> {
        return mediaRouter.routes
            .filter { it.isEnabled }
            .map {
                AudioOutputDevice(
                    name = it.name.toString(),
                    isConnected = it.isConnecting || it.isSelected,
                    id = it.id
                )
            }
    }

    fun selectDevice(deviceId: String) {
        mediaRouter.routes.firstOrNull { it.id == deviceId }?.let { mediaRouter.selectRoute(it) }
    }


    fun observeRouteChanges(onChange: () -> Unit) {
        val selector = MediaRouteSelector.Builder().build()

        mediaRouter.addCallback(
            selector,
            object : MediaRouter.Callback() {
                override fun onRouteSelected(router: MediaRouter, route: RouteInfo) = onChange()
                override fun onRouteUnselected(router: MediaRouter, route: RouteInfo) {
                    val fallback = mediaRouter.defaultRoute
                    mediaRouter.selectRoute(fallback)
                    onChange()
                }
                override fun onRouteChanged(router: MediaRouter, route: RouteInfo) = onChange()
            },
            MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN
        )
    }
}