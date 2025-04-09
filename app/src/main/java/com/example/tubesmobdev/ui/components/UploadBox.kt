package com.example.tubesmobdev.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.tubesmobdev.util.SongUtil

@Composable
fun UploadBox(label: String, onClick: () -> Unit, uri: Uri?, icon: ImageVector, duration: Long?) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(150.dp)
            .padding(8.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color(0xff535353),
                    style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 20f), 0f))
                )
            },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp),
    ) {

        if (uri != null) {
            if (SongUtil.isAudioFile(LocalContext.current, uri)){
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(label, fontSize = 12.sp, color = Color(0xff535353))
                    Spacer(Modifier.height(15.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color(0xff535353),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(15.dp))
                    duration?.let {
                        Text(
                            text = "Duration: ${SongUtil.formatDuration(it)}",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 12.sp, color = Color(0xff535353))
                Spacer(Modifier.height(15.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xff535353),
                    modifier = Modifier.size(40.dp)
                )
                duration?.let {
                    Text(
                        text = "Duration: ${SongUtil.formatDuration(it)}",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }


}