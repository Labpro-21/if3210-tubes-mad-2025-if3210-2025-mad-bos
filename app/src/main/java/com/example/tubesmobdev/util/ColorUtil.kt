package com.example.tubesmobdev.util

import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest

@Composable
fun rememberDominantColor(
    imageUrl: String,
    defaultColor: Color = Color(0xFF3B0B17)
): Color {
    val context = LocalContext.current
    val dominantColor by produceState<Color>(initialValue = defaultColor, key1 = imageUrl) {
        // Build an image request with hardware bitmaps disabled (harus non-hardware untuk Palette)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()
        val result = context.imageLoader.execute(request)
        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
        val palette = bitmap?.let { Palette.from(it).generate() }
        val colorInt = palette?.getDominantColor(defaultColor.toArgb()) ?: defaultColor.toArgb()
        value = Color(colorInt)
    }
    return dominantColor
}