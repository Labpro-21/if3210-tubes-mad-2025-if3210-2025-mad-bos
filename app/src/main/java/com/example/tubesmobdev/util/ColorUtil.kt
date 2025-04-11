package com.example.tubesmobdev.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.example.tubesmobdev.R

@Composable
fun rememberDominantColor(
    imageUrl: String,
    defaultColor: Color = Color(0xFF3B0B17)
): Color {
    val context = LocalContext.current
    val dominantColor by produceState<Color>(initialValue = defaultColor, key1 = imageUrl) {
        val bitmap: Bitmap? = if (imageUrl.isEmpty()) {
            (ContextCompat.getDrawable(context, R.drawable.music) as? BitmapDrawable)?.bitmap
        } else {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            (result.drawable as? BitmapDrawable)?.bitmap
        }

        val palette = bitmap?.let { Palette.from(it).generate() }
        val colorInt = palette?.getDominantColor(defaultColor.toArgb()) ?: defaultColor.toArgb()
        value = Color(colorInt)
    }
    return dominantColor
}