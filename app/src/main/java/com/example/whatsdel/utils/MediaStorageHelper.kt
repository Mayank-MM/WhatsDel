package com.example.whatsdel.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object MediaStorageHelper {

    private const val TAG = "MediaStorageHelper"
    private const val THUMBNAILS_DIR = "thumbnails"
    private const val WHATSDEL_MEDIA_DIR = "WhatsDelMedia"

    /**
     * Sanitizes group chat names by stripping WhatsApp notification message count suffixes e.g., " (4 messages)", " (2 new messages)".
     */
    fun sanitizeChatName(name: String): String {
        if (name.isBlank()) return name
        return name.replace(Regex("""\s*\(\d+\s*(?:messages?|new messages?|unread)?\)""", RegexOption.IGNORE_CASE), "").trim()
    }

    /**
     * Finds all existing WhatsApp Media root directories on the device.
     */
    fun getWhatsAppMediaRootDirectories(): List<File> {
        val storageDir = Environment.getExternalStorageDirectory()
        val candidatePaths = listOf(
            "Android/media/com.whatsapp/WhatsApp/Media",
            "WhatsApp/Media",
            "Android/media/com.whatsapp.w4b/WhatsApp Business/Media",
            "WhatsApp Business/Media"
        )

        return candidatePaths
            .map { File(storageDir, it) }
            .filter { it.exists() && it.isDirectory }
    }

    /**
     * Gets or creates the app-private WhatsDelMedia storage directory.
     */
    fun getWhatsDelMediaDirectory(context: Context): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), WHATSDEL_MEDIA_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Detects media type from file extension or parent directory name.
     * Returns: "image", "video", "voice_note", or null if not a targeted media file.
     */
    fun detectMediaTypeFromFile(file: File): String? {
        val fileName = file.name.uppercase()
        val ext = file.extension.lowercase()
        val parentName = file.parentFile?.name?.lowercase() ?: ""
        val grandParentName = file.parentFile?.parentFile?.name?.lowercase() ?: ""

        // Check filename prefixes used by WhatsApp
        if (fileName.startsWith("PTT-") || fileName.startsWith("AUD-")) {
            return "voice_note"
        }
        if (fileName.startsWith("VID-") || fileName.startsWith("VID_")) {
            return "video"
        }
        if (fileName.startsWith("IMG-") || fileName.startsWith("IMG_")) {
            return "image"
        }
        if (fileName.startsWith("DOC-") || fileName.startsWith("DOC_")) {
            return "document"
        }

        // Check directory hierarchy
        if (parentName.contains("voice") || parentName.contains("audio") ||
            grandParentName.contains("voice") || grandParentName.contains("audio")) {
            return "voice_note"
        }
        if (parentName.contains("image") || parentName.contains("photo") ||
            grandParentName.contains("image") || grandParentName.contains("photo")) {
            return "image"
        }
        if (parentName.contains("video") || grandParentName.contains("video")) {
            return "video"
        }
        if (parentName.contains("document") || parentName.contains("doc") ||
            grandParentName.contains("document") || grandParentName.contains("doc")) {
            return "document"
        }

        return when (ext) {
            "jpg", "jpeg", "png", "webp" -> "image"
            "mp4", "3gp", "mkv", "avi" -> "video"
            "opus", "ogg", "m4a", "aac", "mp3" -> "voice_note"
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar", "apk", "csv", "rtf", "epub" -> "document"
            else -> null
        }
    }

    /**
     * Copies a source file to destination in app-private storage.
     * Returns Pair(absolutePath, fileSize) on success, or null on failure.
     */
    fun copyFileToAppStorage(srcFile: File, destFile: File): Pair<String, Long>? {
        return try {
            if (!srcFile.exists() || !srcFile.canRead()) return null
            destFile.parentFile?.let { if (!it.exists()) it.mkdirs() }

            FileInputStream(srcFile).use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.d(TAG, "Copied file: ${srcFile.name} -> ${destFile.absolutePath} (${destFile.length()} bytes)")
            Pair(destFile.absolutePath, destFile.length())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy file: ${srcFile.absolutePath}", e)
            null
        }
    }

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
        val ext = when (type) {
            "image" -> "jpg"
            "video" -> "mp4"
            "voice_note" -> "opus"
            else -> "jpg"
        }
        return "${sanitized}_${timestamp}_$type.$ext"
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
     * Securely copies a content URI to app-private storage.
     * Returns a Pair of (absolutePath, fileSize) on success, or throws an Exception on failure.
     */
    fun copyUriToAppStorage(context: Context, uri: android.net.Uri, fileName: String): Pair<String, Long> {
        val dir = getWhatsDelMediaDirectory(context)
        val destFile = File(dir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw java.io.FileNotFoundException("Could not open InputStream for URI: $uri")

        return Pair(destFile.absolutePath, destFile.length())
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
