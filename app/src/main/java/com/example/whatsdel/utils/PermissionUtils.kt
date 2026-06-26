package com.example.whatsdel.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings

object PermissionUtils {

    /**
     * Checks if the app's NotificationListenerService is enabled in system settings.
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return flat.split(":").any {
            val componentName = ComponentName.unflattenFromString(it)
            componentName != null && componentName.packageName == packageName
        }
    }

    /**
     * Checks if storage permission is granted.
     * On Android 11+ (API 30), checks MANAGE_EXTERNAL_STORAGE.
     * On older versions, checks READ_EXTERNAL_STORAGE.
     */
    fun isStoragePermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
            context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if battery optimization is disabled for this app.
     */
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Returns an Intent to open the Notification Listener settings page.
     */
    fun notificationListenerSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Returns an Intent to open the appropriate storage settings page.
     */
    fun storageSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    /**
     * Returns an Intent to open the Battery Optimization settings page.
     */
    fun batteryOptimizationIntent(): Intent {
        return Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
