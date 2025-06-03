package com.noobdev.numlexambuddy.data.download

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

/**
 * A wrapper class for Android's DownloadManager to handle document downloads.
 */
class DownloadManagerWrapper(private val context: Context) {

    private val downloadManager: DownloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    companion object {
        private const val TAG = "DownloadManagerWrapper"
    }    // Track retry attempts
    private val retryAttempts = mutableMapOf<Long, Int>()
    
    /**
     * Initiates download of a document from a URL.
     *
     * @param document The document to be downloaded
     * @param retryCount Optional retry attempt count for retried downloads
     * @return The download ID assigned by the DownloadManager, or -1 if download couldn't be initiated
     */
    fun startDownload(document: Document, retryCount: Int = 0): Long {
        try {
            if (document.sourceUrl.isNullOrBlank()) {
                Log.e(TAG, "Cannot download document with empty URL")
                return -1
            }
            
            val uri = Uri.parse(document.sourceUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle(document.title)
                setDescription("Downloading document")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                
                // Set destination to a temporary file
                val destinationUri = Uri.fromFile(File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), 
                    "${document.id}_temp_${document.title}"))
                setDestinationUri(destinationUri)
                
                // Allow downloads over cellular/wifi networks
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                
                // Add network timeout
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }
            
            return downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting download: ${e.message}", e)
            return -1L
        }
    }

    /**
     * Queries the status of a download.
     *
     * @param downloadId The ID of the download to check
     * @return A DownloadStatus object with information about the download
     */
    fun queryDownloadStatus(downloadId: Long): DownloadStatus {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        
        if (!cursor.moveToFirst()) {
            cursor.close()
            return DownloadStatus.Failed("Download not found")
        }
        
        return try {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
            val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val fileUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

            val status = cursor.getInt(statusIndex)
            val reason = if (reasonIndex != -1) cursor.getInt(reasonIndex) else 0
            val bytesDownloaded = if (bytesDownloadedIndex != -1) cursor.getLong(bytesDownloadedIndex) else 0
            val bytesTotal = if (bytesTotalIndex != -1) cursor.getLong(bytesTotalIndex) else 0
            val fileUri = if (fileUriIndex != -1) cursor.getString(fileUriIndex) else null

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    DownloadStatus.Completed(bytesDownloaded, bytesTotal, fileUri)
                }
                DownloadManager.STATUS_FAILED -> {
                    val errorMessage = getDownloadErrorMessage(reason)
                    DownloadStatus.Failed(errorMessage)
                }
                DownloadManager.STATUS_PAUSED -> {
                    val pausedMessage = getDownloadPausedMessage(reason)
                    DownloadStatus.Paused(pausedMessage, bytesDownloaded, bytesTotal)
                }
                DownloadManager.STATUS_PENDING -> {
                    DownloadStatus.Pending
                }
                DownloadManager.STATUS_RUNNING -> {
                    DownloadStatus.InProgress(bytesDownloaded, bytesTotal)
                }
                else -> {
                    DownloadStatus.Failed("Unknown status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying download status", e)
            DownloadStatus.Failed("Error querying download: ${e.message}")
        } finally {
            cursor.close()
        }
    }

    /**
     * Provides a flow of download progress updates.
     *
     * @param downloadId The ID of the download to observe
     * @param pollingIntervalMs How often to check for updates (in milliseconds)
     * @return Flow emitting DownloadStatus updates
     */
    fun observeDownload(downloadId: Long, pollingIntervalMs: Long = 500): Flow<DownloadStatus> = flow {
        var isDownloading = true
        var lastStatus: DownloadStatus? = null

        while (isDownloading) {
            val status = queryDownloadStatus(downloadId)
            
            // Only emit if status changed or progress updated
            if (status != lastStatus) {
                emit(status)
                lastStatus = status
            }
            
            // Stop polling when download is completed or failed
            if (status is DownloadStatus.Completed || status is DownloadStatus.Failed) {
                isDownloading = false
            }
            
            kotlinx.coroutines.delay(pollingIntervalMs)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Cancels an ongoing download.
     *
     * @param downloadId The ID of the download to cancel
     * @return true if the download was canceled successfully, false otherwise
     */
    fun cancelDownload(downloadId: Long): Boolean {
        return try {
            val result = downloadManager.remove(downloadId)
            result == 1
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling download", e)
            false
        }
    }

    /**
     * Retrieves the local URI for a completed download.
     *
     * @param downloadId The ID of the completed download
     * @return The local URI as a string, or null if not found
     */
    fun getDownloadUri(downloadId: Long): String? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        
        return try {
            if (!cursor.moveToFirst()) {
                return null
            }
            
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            if (columnIndex != -1) {
                cursor.getString(columnIndex)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving download URI", e)
            null
        } finally {
            cursor.close()
        }
    }

    /**
     * Translates a DownloadManager error code into a human-readable message.
     */
    private fun getDownloadErrorMessage(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Storage device not found"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            DownloadManager.ERROR_FILE_ERROR -> "File error"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient storage space"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
            DownloadManager.ERROR_UNKNOWN -> "Unknown error"
            else -> "Error code: $reason"
        }
    }

    /**
     * Translates a DownloadManager paused reason code into a human-readable message.
     */
    private fun getDownloadPausedMessage(reason: Int): String {
        return when (reason) {
            DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Waiting for Wi-Fi connection"
            DownloadManager.PAUSED_UNKNOWN -> "Download paused"
            DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "Waiting for network connection"
            DownloadManager.PAUSED_WAITING_TO_RETRY -> "Waiting to retry"
            else -> "Paused (reason code: $reason)"
        }
    }
    
    /**
     * Handles retry logic when a download fails
     *
     * @param document The document that failed to download
     * @param downloadId The ID of the failed download
     * @param statusCode The error code from DownloadManager
     * @return A new download ID if retry initiated, or -1 if not retrying
     */
    fun handleDownloadError(document: Document, downloadId: Long, statusCode: Int): Long {
        val currentRetries = retryAttempts[downloadId] ?: 0
        val maxRetries = when (statusCode) {
            DownloadManager.ERROR_HTTP_DATA_ERROR -> 3
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> 2
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> 1
            else -> 0 // Don't retry other errors by default
        }
        
        return if (currentRetries < maxRetries) {
            // Remove the failed download
            downloadManager.remove(downloadId)
            
            // Start a new download attempt
            val newDownloadId = startDownload(document, currentRetries + 1)
            
            // Track retry count for the new download
            if (newDownloadId != -1L) {
                retryAttempts[newDownloadId] = currentRetries + 1
                Log.d(TAG, "Retrying download for ${document.title}, attempt ${currentRetries + 1}")
            }
            
            newDownloadId
        } else {
            // Clean up after max retries
            retryAttempts.remove(downloadId)
            Log.e(TAG, "Max retry attempts reached for download ${document.title}")
            -1
        }
    }
    
    /**
     * Cleans up tracking for a successful download
     */
    fun onDownloadComplete(downloadId: Long) {
        retryAttempts.remove(downloadId)
    }
    
    /**
     * Checks network conditions before starting download
     * 
     * @return True if network is suitable for download, false otherwise
     */
    fun isNetworkSuitableForDownload(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        
        return capabilities != null && (
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}

/**
 * Represents the current status of a download.
 */
sealed class DownloadStatus {
    object Pending : DownloadStatus()
    
    data class InProgress(
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : DownloadStatus() {
        val progress: Float
            get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
    }
    
    data class Paused(
        val reason: String,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : DownloadStatus()
    
    data class Completed(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val fileUri: String?
    ) : DownloadStatus()
    
    data class Failed(val reason: String) : DownloadStatus()
}
