package com.example.whatsdel.utils

import android.content.Context
import android.os.Build
import android.os.FileObserver
import android.util.Log
import com.example.whatsdel.domain.repository.MessageRepository
import java.io.File

class WhatsAppMediaObserver(
    private val context: Context,
    private val messageRepository: MessageRepository? = null
) {

    private val activeObservers = mutableListOf<FileObserver>()

    companion object {
        private const val TAG = "WhatsAppMediaObserver"
    }

    /**
     * Starts watching all available WhatsApp Media directories for real-time changes.
     */
    fun startWatching() {
        stopWatching()

        val rootDirs = MediaStorageHelper.getWhatsAppMediaRootDirectories()
        if (rootDirs.isEmpty()) {
            Log.w(TAG, "No WhatsApp media root directories found to watch.")
            return
        }

        for (rootDir in rootDirs) {
            val subDirs = rootDir.listFiles() ?: continue
            for (subDir in subDirs) {
                if (!subDir.isDirectory) continue
                val nameLower = subDir.name.lowercase()
                if (nameLower.contains("image") || nameLower.contains("video") ||
                    nameLower.contains("voice") || nameLower.contains("audio") ||
                    nameLower.contains("document") || nameLower.contains("doc")) {

                    registerObserverForDirectory(subDir)

                    // Also watch immediate subdirectories (excluding "Sent")
                    val childFolders = subDir.listFiles() ?: continue
                    for (childFolder in childFolders) {
                        if (childFolder.isDirectory && !childFolder.name.equals("Sent", ignoreCase = true)) {
                            registerObserverForDirectory(childFolder)
                        }
                    }
                }
            }
        }

        Log.i(TAG, "Started ${activeObservers.size} WhatsApp media FileObservers.")
    }

    private fun registerObserverForDirectory(dir: File) {
        try {
            val events = FileObserver.CREATE or FileObserver.CLOSE_WRITE or FileObserver.MOVED_TO

            val observer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                object : FileObserver(dir, events) {
                    override fun onEvent(event: Int, path: String?) {
                        if (path != null) {
                            handleFileEvent(dir, path)
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                object : FileObserver(dir.absolutePath, events) {
                    override fun onEvent(event: Int, path: String?) {
                        if (path != null) {
                            handleFileEvent(dir, path)
                        }
                    }
                }
            }

            observer.startWatching()
            activeObservers.add(observer)
            Log.d(TAG, "Watching directory: ${dir.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start FileObserver for ${dir.absolutePath}", e)
        }
    }

    private fun handleFileEvent(parentDir: File, fileName: String) {
        val file = File(parentDir, fileName)
        if (file.isDirectory) {
            if (!file.name.equals("Sent", ignoreCase = true)) {
                Log.d(TAG, "New subfolder detected, registering observer: ${file.absolutePath}")
                registerObserverForDirectory(file)
            }
        } else if (file.isFile) {
            Log.d(TAG, "New WhatsApp media event detected: ${file.name}")
            WhatsAppMediaBackupManager.backupMediaFile(context, file, messageRepository)
        }
    }

    /**
     * Stops all active FileObservers.
     */
    fun stopWatching() {
        for (observer in activeObservers) {
            try {
                observer.stopWatching()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping FileObserver", e)
            }
        }
        activeObservers.clear()
        Log.d(TAG, "Stopped all WhatsApp media FileObservers.")
    }
}
