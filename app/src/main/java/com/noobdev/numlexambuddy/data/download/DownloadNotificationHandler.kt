package com.noobdev.numlexambuddy.data.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.noobdev.numlexambuddy.MainActivity
import com.noobdev.numlexambuddy.R
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentStatus

/**
 * Handles notifications for document downloads.
 */
class DownloadNotificationHandler(
    private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "document_downloads"
        private const val DOWNLOAD_PROGRESS_GROUP = "download_progress_group"
        private const val DOWNLOAD_COMPLETE_GROUP = "download_complete_group"
        
        // IDs for notifications
        private const val DOWNLOAD_PROGRESS_ID = 1000
        private const val DOWNLOAD_COMPLETE_BASE_ID = 2000
        
        // Request codes for PendingIntents
        private const val REQUEST_CODE_OPEN_DOCUMENT = 3000
    }
    
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService()!!
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Download progress channel
            val progressChannel = NotificationChannel(
                CHANNEL_ID,
                "Document Downloads",
                NotificationManager.IMPORTANCE_LOW // Low importance to avoid sound for progress updates
            ).apply {
                description = "Shows progress of document downloads"
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannel(progressChannel)
        }
    }
    
    /**
     * Shows a notification for download progress
     */
    fun showDownloadProgressNotification(document: Document, progress: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a download icon
            .setContentTitle("Downloading ${document.title}")
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setGroup(DOWNLOAD_PROGRESS_GROUP)
            
        notificationManager.notify(DOWNLOAD_PROGRESS_ID + document.id.toInt(), builder.build())
    }
    
    /**
     * Shows a notification for a completed download
     */
    fun showDownloadCompletedNotification(document: Document) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("DOCUMENT_ID", document.id)
            putExtra("ACTION", "OPEN_DOCUMENT")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_DOCUMENT + document.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a document icon
            .setContentTitle("Download Complete")
            .setContentText("${document.title} has been downloaded")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(DOWNLOAD_COMPLETE_GROUP)
            
        // Cancel any ongoing progress notification
        notificationManager.cancel(DOWNLOAD_PROGRESS_ID + document.id.toInt())
        
        // Show completed notification
        notificationManager.notify(DOWNLOAD_COMPLETE_BASE_ID + document.id.toInt(), builder.build())
    }
    
    /**
     * Shows a notification for a failed download
     */
    fun showDownloadFailedNotification(document: Document, errorMessage: String?) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with an error icon
            .setContentTitle("Download Failed")
            .setContentText("Failed to download ${document.title}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Failed to download ${document.title}${errorMessage?.let { ": $it" } ?: ""}")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            
        // Cancel any ongoing progress notification
        notificationManager.cancel(DOWNLOAD_PROGRESS_ID + document.id.toInt())
        
        // Show failed notification
        notificationManager.notify(DOWNLOAD_COMPLETE_BASE_ID + document.id.toInt(), builder.build())
    }
    
    /**
     * Cancels all notifications related to downloads
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
