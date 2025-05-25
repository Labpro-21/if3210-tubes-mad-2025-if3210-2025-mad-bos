package com.example.tubesmobdev.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tubesmobdev.ui.viewmodel.ProfileViewModel
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap
import com.example.tubesmobdev.data.model.SoundCapsuleShareData
import com.example.tubesmobdev.data.model.SoundCapsuleStreakShareData
import kotlinx.coroutines.delay
@Composable
fun SoundCapsuleSharePage(
    month: String,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    isStreak: Boolean = false,
) {
    val context = LocalContext.current
    val captureRef = remember { mutableStateOf<ComposeView?>(null) }
    var hasCaptured by remember { mutableStateOf(false) }

    val shareData by produceState<Any?>(initialValue = null, month) {
        viewModel.fetchProfile()
        delay(300)
        value = if (isStreak) {
            viewModel.getStreakCapsuleShareData(month)
        } else {
            viewModel.getSoundCapsuleShareData(month)
        }
        delay(300)
    }

    LaunchedEffect(shareData, captureRef.value) {
        if (shareData != null && captureRef.value != null && !hasCaptured) {
            delay(800L)
            hasCaptured = true
            val bitmap = captureRef.value!!.drawToBitmap()
            val filenamePrefix = if (isStreak) "share_streak" else "share_capsule"
            shareBitmap(context, bitmap, filenamePrefix)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                color = Color(0xFF1C1C1E)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Share",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            if (shareData != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        factory = { context ->
                            ComposeView(context).apply {
                                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                                setContent {
                                    if (isStreak && shareData is SoundCapsuleStreakShareData) {
                                        val streak = shareData as SoundCapsuleStreakShareData
                                        SoundCapsuleShareStreakContent(
                                           data = streak
                                        )
                                    } else if (!isStreak && shareData is SoundCapsuleShareData) {
                                        SoundCapsuleShareContent(shareData as SoundCapsuleShareData)
                                    }
                                }
                                postDelayed({
                                    captureRef.value = this
                                }, 400)
                            }
                        }
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

fun shareBitmap(context: Context, bitmap: Bitmap, filenamePrefix: String = "share_capsule") {
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()

    val fileName = "${filenamePrefix}_${System.currentTimeMillis()}.png"
    val file = File(cachePath, fileName)

    FileOutputStream(file).use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }

    val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}