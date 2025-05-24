package com.example.tubesmobdev.util

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast

fun exportCsvFile(context: Context, content: String, filename: String) {
    val resolver = context.contentResolver
    val csvFileName = filename

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, csvFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Download")
    }

    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    if (uri != null) {
        resolver.openOutputStream(uri).use { outputStream ->
            outputStream?.write(content.toByteArray())
        }
        Toast.makeText(context, "Downloaded to Downloads/$csvFileName", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Failed to export CSV", Toast.LENGTH_SHORT).show()
    }
}
