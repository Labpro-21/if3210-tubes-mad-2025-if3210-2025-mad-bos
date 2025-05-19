package com.example.tubesmobdev.service

import android.graphics.Bitmap
import android.graphics.Color
import com.example.tubesmobdev.data.model.Song
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun generateQRCodeUrl(song: Song): Bitmap {
    val songUri = "purrytify://song/${song.serverId}"
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(songUri, BarcodeFormat.QR_CODE, 512, 512)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }

    return bitmap
}