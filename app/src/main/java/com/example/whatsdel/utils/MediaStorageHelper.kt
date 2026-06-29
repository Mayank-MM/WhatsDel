package com.example.whatsdel.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object MediaStorageHelper {

    private const val TAG = "MediaStorageHelper"
    private const val THUMBNAILS_DIR = "thumbnails"

    /**
     * Saves a notification bitmap thumbnail to app-private storage.
     * Returns the absolute path of the saved file, or null on failure.
     */
    fun saveThumbnail(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val dir = File(context.filesDir, THUMBNAILS_DIR)
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, fileName)
            if (file.exists()) {
                Log.d(TAG, "Thumbnail already exists: ${file.absolutePath}")
                return file.absolutePath
            }

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            Log.d(TAG, "Thumbnail saved: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save thumbnail", e)
            null
        }
    }

    /**
     * Generates a unique filename from sender, timestamp, and type.
     */
    fun generateFileName(sender: String, timestamp: Long, type: String): String {
        val sanitized = sender.replace(Regex("[^a-zA-Z0-9]"), "_").take(20)
        return "${sanitized}_${timestamp}_$type.jpg"
    }

    /**
     * Deletes a thumbnail file from app storage.
     */
    fun deleteThumbnail(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete thumbnail: $path", e)
            false
        }
    }

    /**
     * Returns the total size of the thumbnails directory in bytes.
     */
    fun getThumbnailsDirSize(context: Context): Long {
        val dir = File(context.filesDir, THUMBNAILS_DIR)
        if (!dir.exists()) return 0
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }
}
