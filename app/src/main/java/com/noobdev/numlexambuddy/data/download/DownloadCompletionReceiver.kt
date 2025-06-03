package com.noobdev.numlexambuddy.data.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A BroadcastReceiver that listens for download completion events.
 * This class should be registered when downloads are initiated and unregistered when no longer needed.
 */
class DownloadCompletionReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "DownloadCompletionReceiver"
    }
    
    // Flow that emits download IDs when they complete
    private val _downloadCompletions = MutableSharedFlow<DownloadResult>(replay = 0)
    val downloadCompletions: SharedFlow<DownloadResult> = _downloadCompletions.asSharedFlow()
    
    // Map to track download IDs and their associated document IDs for correlation
    private val downloadMap = mutableMapOf<Long, String>()

    /**
     * Associates a download ID with a document ID for tracking.
     */
    fun trackDownload(downloadId: Long, documentId: String) {
        downloadMap[downloadId] = documentId
    }
    
    /**
     * Removes tracking for a download ID.
     */
    fun stopTracking(downloadId: Long) {
        downloadMap.remove(downloadId)
    }
    
    /**
     * Clears all tracked downloads.
     */
    fun clearTracking() {
        downloadMap.clear()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            
            if (downloadId == -1L) {
                Log.e(TAG, "Invalid download ID received")
                return
            }
            
            val documentId = downloadMap[downloadId]
            
            if (documentId == null) {
                Log.w(TAG, "Received completion for untracked download ID: $downloadId")
                return
            }
            
            // Query the download status
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            
            try {
                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusIndex)
                    
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            val localUri = if (uriIndex != -1) cursor.getString(uriIndex) else null
                            
                            if (localUri != null) {
                                val contentLength = try {
                                    val sizeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                                    if (sizeIndex != -1) cursor.getLong(sizeIndex) else 0
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to get download size", e)
                                    0L
                                }
                                
                                // Parse the URI string to a Uri object
                                val uri = try {
                                    Uri.parse(localUri)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to parse download URI: $localUri", e)
                                    null
                                }
                                
                                if (uri != null) {
                                    // Check if the file is valid (not empty or corrupt)
                                    val file = uri.path?.let { java.io.File(it) }
                                    if (file != null && file.exists() && file.length() > 0) {
                                        // Flow emit: success
                                        _downloadCompletions.tryEmit(
                                            DownloadResult.Success(
                                                downloadId = downloadId,
                                                documentId = documentId,
                                                uri = uri,
                                                size = contentLength
                                            )
                                        )
                                    } else {
                                        // Flow emit: failure (invalid URI)
                                        _downloadCompletions.tryEmit(
                                            DownloadResult.Error(
                                                downloadId = downloadId,
                                                documentId = documentId,
                                                error = "Invalid download URI",
                                                isRetryable = true,
                                                errorCode = DownloadManager.ERROR_FILE_ERROR
                                            )
                                        )
                                    }
                                } else {
                                    // Flow emit: failure (no URI)
                                    _downloadCompletions.tryEmit(
                                        DownloadResult.Error(
                                            downloadId = downloadId,
                                            documentId = documentId,
                                            error = "No download URI found",
                                            isRetryable = false,
                                            errorCode = DownloadManager.ERROR_FILE_ERROR
                                        )
                                    )
                                }
                            }
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val reason = if (reasonIndex != -1) cursor.getInt(reasonIndex) else -1
                            val errorMessage = getErrorMessage(reason)
                            
                            // Check if this error is retryable
                            val isRetryable = when (reason) {
                                DownloadManager.ERROR_HTTP_DATA_ERROR,
                                DownloadManager.ERROR_UNHANDLED_HTTP_CODE,
                                DownloadManager.ERROR_TOO_MANY_REDIRECTS,
                                DownloadManager.ERROR_UNKNOWN -> true
                                else -> false
                            }
                            
                            // Flow emit: failure with retry info
                            _downloadCompletions.tryEmit(
                                DownloadResult.Error(
                                    downloadId = downloadId,
                                    isRetryable = isRetryable,
                                    errorCode = reason,
                                    documentId = documentId,
                                    error = errorMessage
                                )
                            )
                        }
                        else -> {
                            // Unexpected status
                            _downloadCompletions.tryEmit(
                                DownloadResult.Error(
                                    downloadId = downloadId,
                                    documentId = documentId,
                                    error = "Unexpected download status: $status",
                                    isRetryable = true,
                                    errorCode = DownloadManager.ERROR_UNKNOWN
                                )
                            )
                        }
                    }
                } else {
                    // No record found
                    _downloadCompletions.tryEmit(
                        DownloadResult.Error(
                            downloadId = downloadId,
                            documentId = documentId,
                            error = "No download record found"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling download completion", e)
                // Flow emit: exception
                _downloadCompletions.tryEmit(
                    DownloadResult.Error(
                        downloadId = downloadId,
                        documentId = documentId,
                        error = "Error: ${e.message}"
                    )
                )
            } finally {
                cursor.close()
                // Remove this download from tracking
                stopTracking(downloadId)
            }
        }
    }

    /**
     * Get a human-readable error message for a download error code.
     */
    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Storage device not found"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            DownloadManager.ERROR_FILE_ERROR -> "File error"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient storage space"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
            DownloadManager.ERROR_UNKNOWN -> "Unknown error"
            else -> "Error code: $errorCode"
        }
    }

    /**
     * Register this receiver for download completion events.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun register(context: Context) {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(this, filter, Context.RECEIVER_NOT_EXPORTED)    }

    /**
     * Unregister this receiver.
     */
    fun unregister(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered, ignore
        }
    }
}

/**
 * Represents the result of a download operation.
 */
sealed class DownloadResult {
    data class Success(
        val downloadId: Long,
        val documentId: String,
        val uri: Uri,
        val size: Long
    ) : DownloadResult()
      data class Error(
        val downloadId: Long,
        val documentId: String,
        val error: String,
        val isRetryable: Boolean = false,
        val errorCode: Int = 0
    ) : DownloadResult()
}
