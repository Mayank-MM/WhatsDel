package com.example.whatsdel.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.util.Log
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

object WhatsAppMediaBackupManager {

    private const val TAG = "MediaBackupManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Backs up a media file from WhatsApp into WhatsDel private storage and links it to DB.
     */
    fun backupMediaFile(context: Context, srcFile: File, messageRepository: MessageRepository? = null, retryCount: Int = 0) {
        if (!srcFile.exists() || !srcFile.isFile) return

        val pathLower = srcFile.absolutePath.lowercase()
        // Ignore files in Sent directories or temp/hidden files
        if (pathLower.contains("/sent/") || srcFile.name.startsWith(".") || srcFile.name.endsWith(".tmp")) {
            return
        }

        // If file is currently being written (0 bytes), retry after a short delay
        if (srcFile.length() <= 0) {
            if (retryCount < 3) {
                scope.launch {
                    kotlinx.coroutines.delay(800)
                    backupMediaFile(context, srcFile, messageRepository, retryCount + 1)
                }
            }
            return
        }

        val mediaType = MediaStorageHelper.detectMediaTypeFromFile(srcFile) ?: return

        val destDir = MediaStorageHelper.getWhatsDelMediaDirectory(context)
        val destFile = File(destDir, "whatsdel_${srcFile.lastModified()}_${srcFile.name}")

        // Skip if already backed up
        if (destFile.exists() && destFile.length() == srcFile.length()) {
            return
        }

        val copyResult = MediaStorageHelper.copyFileToAppStorage(srcFile, destFile) ?: return
        Log.i(TAG, "Successfully backed up $mediaType: ${srcFile.name} (${copyResult.second} bytes)")

        if (messageRepository != null) {
            scope.launch {
                try {
                    linkBackupToDatabase(
                        context = context,
                        backupFile = destFile,
                        mediaType = mediaType,
                        timestamp = srcFile.lastModified(),
                        fileSize = copyResult.second,
                        repository = messageRepository
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to link backed-up media to database", e)
                }
            }
        }
    }

    /**
     * Links a backed-up media file to an existing or new MessageEntity.
     */
    private suspend fun linkBackupToDatabase(
        context: Context,
        backupFile: File,
        mediaType: String,
        timestamp: Long,
        fileSize: Long,
        repository: MessageRepository
    ) {
        // Try to generate thumbnail for images and videos
        var thumbnailPath: String? = null
        try {
            val thumbBitmap = when (mediaType) {
                "image" -> BitmapFactory.decodeFile(backupFile.absolutePath)
                "video" -> ThumbnailUtils.createVideoThumbnail(
                    backupFile.absolutePath,
                    MediaStore.Images.Thumbnails.MINI_KIND
                )
                else -> null
            }
            if (thumbBitmap != null) {
                thumbnailPath = MediaStorageHelper.saveThumbnail(
                    context,
                    thumbBitmap,
                    "thumb_backup_${timestamp}_$mediaType.jpg"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating thumbnail for backup", e)
        }

        // Check if there is an unlinked message entity created within 3 minutes of this file's timestamp
        val recentActive = repository.getRecentActiveMessages()
        val existingUnlinked = recentActive.firstOrNull { msg ->
            msg.hasMedia && msg.mediaType == mediaType && msg.localMediaPath == null &&
                    Math.abs(msg.timestamp - timestamp) < 180_000
        }

        if (existingUnlinked != null) {
            repository.updateMessage(
                existingUnlinked.copy(
                    localMediaPath = backupFile.absolutePath,
                    mediaSize = fileSize,
                    thumbnailPath = thumbnailPath ?: existingUnlinked.thumbnailPath,
                    mediaCaptureSuccess = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Log.d(TAG, "Linked backed-up media to existing message ID: ${existingUnlinked.id}")
        } else {
            // Create a new standalone message record for the backed-up media
            val messageEntity = MessageEntity(
                sender = "WhatsApp Media",
                chatName = "Media Backup",
                message = "${mediaType.replaceFirstChar { it.uppercase() }} received",
                timestamp = timestamp,
                packageName = "com.whatsapp",
                notificationId = (timestamp % 100000).toInt(),
                messageType = "media",
                isDeleted = false,
                isEdited = false,
                hasMedia = true,
                mediaType = mediaType,
                mediaFileName = backupFile.name,
                mediaSize = fileSize,
                thumbnailPath = thumbnailPath,
                localMediaPath = backupFile.absolutePath,
                mediaCaptureSuccess = true,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertMessage(messageEntity)
            Log.d(TAG, "Created new MessageEntity for backed-up $mediaType: ${backupFile.name}")
        }
    }

    /**
     * Scans all existing WhatsApp media folders and backs up unbacked-up files.
     */
    fun scanWhatsAppMediaFolders(context: Context, messageRepository: MessageRepository? = null) {
        scope.launch {
            try {
                val rootDirs = MediaStorageHelper.getWhatsAppMediaRootDirectories()
                for (rootDir in rootDirs) {
                    val subDirs = rootDir.listFiles() ?: continue
                    for (subDir in subDirs) {
                        if (!subDir.isDirectory) continue
                        val folderName = subDir.name.lowercase()
                        if (folderName.contains("image") || folderName.contains("video") || folderName.contains("voice") || folderName.contains("audio")) {
                            scanDirectoryRecursively(context, subDir, messageRepository)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning WhatsApp media folders", e)
            }
        }
    }

    private fun scanDirectoryRecursively(context: Context, dir: File, messageRepository: MessageRepository?) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                if (!file.name.equals("Sent", ignoreCase = true)) {
                    scanDirectoryRecursively(context, file, messageRepository)
                }
            } else {
                backupMediaFile(context, file, messageRepository)
            }
        }
    }
}
