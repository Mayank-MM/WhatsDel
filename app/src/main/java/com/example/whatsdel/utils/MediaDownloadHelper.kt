package com.example.whatsdel.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object MediaDownloadHelper {

    private const val TAG = "MediaDownloadHelper"

    /**
     * Exports a backed-up media file from WhatsDel private storage into device public storage (Gallery/Downloads).
     */
    fun downloadMediaToPublicStorage(context: Context, srcFile: File, mediaType: String): Boolean {
        if (!srcFile.exists() || !srcFile.isFile || srcFile.length() <= 0) {
            Toast.makeText(context, "Media file not found on device", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val fileName = srcFile.name
            val type = mediaType.lowercase()

            val targetFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)

                    when (type) {
                        "image" -> {
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/WhatsDel Downloads")
                        }
                        "video" -> {
                            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/WhatsDel Downloads")
                        }
                        else -> {
                            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/WhatsDel Downloads")
                        }
                    }
                }

                val collection = when (type) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
                }

                val itemUri = resolver.insert(collection, contentValues)
                    ?: throw java.io.IOException("Failed to create MediaStore entry")

                resolver.openOutputStream(itemUri)?.use { out ->
                    FileInputStream(srcFile).use { inStream ->
                        inStream.copyTo(out)
                    }
                } ?: throw java.io.IOException("Failed to open output stream for $itemUri")

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)

                null
            } else {
                // Legacy public storage copy
                val publicDir = when (type) {
                    "image" -> File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "WhatsDel Downloads")
                    "video" -> File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "WhatsDel Downloads")
                    else -> File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "WhatsDel Downloads")
                }
                if (!publicDir.exists()) publicDir.mkdirs()

                val destFile = File(publicDir, fileName)
                FileInputStream(srcFile).use { inStream ->
                    FileOutputStream(destFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
                destFile
            }

            // Trigger MediaScanner so Gallery immediately indexes the newly saved file
            if (targetFile != null) {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetFile.absolutePath),
                    null
                ) { path, uri ->
                    Log.d(TAG, "MediaScanner scanned $path -> $uri")
                }
            }

            val targetFolder = when (type) {
                "image", "video" -> "Gallery"
                else -> "Downloads"
            }
            Toast.makeText(context, "Saved to $targetFolder!", Toast.LENGTH_LONG).show()
            Log.i(TAG, "Successfully exported media file: $fileName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download media file: ${srcFile.name}", e)
            Toast.makeText(context, "Failed to save media: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
