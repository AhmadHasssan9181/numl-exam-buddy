package com.noobdev.numlexambuddy.data.download

import android.content.Context
import android.net.Uri
import android.util.Log
import com.noobdev.numlexambuddy.data.storage.DocumentStorageManager
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentStatus
import com.noobdev.numlexambuddy.utils.FileStorageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * Service responsible for managing document downloads and tracking their status.
 */
class DocumentDownloadService(
    private val context: Context,
    private val downloadManager: DownloadManagerWrapper,
    private val storageManager: DocumentStorageManager
) {
    companion object {
        private const val TAG = "DocumentDownloadService"
    }
    
    // Coroutine scope for async operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Broadcast receiver for download completions
    private val downloadReceiver = DownloadCompletionReceiver()
    
    // Map of download IDs to document IDs
    private val activeDownloads = HashMap<Long, String>()
    
    // Map of document IDs to download status
    private val _downloadStatus = MutableStateFlow<Map<String, DocumentDownloadStatus>>(emptyMap())
    val downloadStatus: StateFlow<Map<String, DocumentDownloadStatus>> = _downloadStatus.asStateFlow()
    
    // Initialize the service
    init {
        // Register the download completion receiver
        downloadReceiver.register(context)
        
        // Collect download completions
        serviceScope.launch {
            downloadReceiver.downloadCompletions.collect { result ->
                when (result) {
                    is DownloadResult.Success -> {
                        handleDownloadSuccess(result)
                    }
                    is DownloadResult.Error -> {
                        handleDownloadError(result)
                    }
                }
            }
        }
    }
    
    /**
     * Start downloading a document.
     *
     * @param document The document to download
     * @return The document with updated status
     */
    suspend fun startDownload(document: Document): Document {
        // Update document status to downloading
        val updatedDocument = storageManager.updateDocumentToDownloading(document)
        
        // Update status map
        updateStatus(document.id, DocumentDownloadStatus.InProgress(0f))
        
        // Start the download
        val downloadId = downloadManager.startDownload(updatedDocument)
        
        if (downloadId != -1L) {
            // Track the download
            activeDownloads[downloadId] = document.id
            downloadReceiver.trackDownload(downloadId, document.id)
            
            // Start observing download progress
            observeDownloadProgress(downloadId, document.id)
            
            return updatedDocument
        } else {
            // Download failed to start
            updateStatus(document.id, DocumentDownloadStatus.Failed("Failed to start download"))
            return document.copy(status = DocumentStatus.FAILED)
        }
    }
    
    /**
     * Observe the progress of a download.
     *
     * @param downloadId The ID of the download
     * @param documentId The ID of the document being downloaded
     */
    private fun observeDownloadProgress(downloadId: Long, documentId: String) {
        serviceScope.launch {
            downloadManager.observeDownload(downloadId).collect { status ->
                when (status) {
                    is DownloadStatus.InProgress -> {
                        updateStatus(
                            documentId,
                            DocumentDownloadStatus.InProgress(status.progress)
                        )
                    }
                    is DownloadStatus.Paused -> {
                        updateStatus(
                            documentId,
                            DocumentDownloadStatus.Paused(status.reason)
                        )
                    }
                    is DownloadStatus.Failed -> {
                        updateStatus(
                            documentId,
                            DocumentDownloadStatus.Failed(status.reason)
                        )
                    }
                    else -> {
                        // Other states are handled by the BroadcastReceiver
                    }
                }
            }
        }
    }
    
    /**
     * Cancel an ongoing download.
     *
     * @param documentId The ID of the document whose download should be canceled
     * @return True if the download was canceled, false otherwise
     */
    fun cancelDownload(documentId: String): Boolean {
        val downloadId = activeDownloads.entries.find { it.value == documentId }?.key
        
        return if (downloadId != null) {
            val result = downloadManager.cancelDownload(downloadId)
            if (result) {
                activeDownloads.remove(downloadId)
                downloadReceiver.stopTracking(downloadId)
                updateStatus(documentId, DocumentDownloadStatus.Canceled)
            }
            result
        } else {
            false
        }
    }
    
    /**
     * Handle a successful download.
     *
     * @param result The download result
     */
    private suspend fun handleDownloadSuccess(result: DownloadResult.Success) {
        try {
            // Get the temporary file from the download
            val downloadUri = result.uri
            
            // Move the file to its final location
            // This is a simplified implementation - in a real app, you might want to
            // move the file to a proper location, validate it, etc.
            val document = processDownloadedFile(result)
            
            // Update status
            updateStatus(result.documentId, DocumentDownloadStatus.Completed)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling successful download", e)
            updateStatus(
                result.documentId,
                DocumentDownloadStatus.Failed("Error processing download: ${e.message}")
            )
        }
    }
    
    /**
     * Handle a failed download.
     *
     * @param result The download result
     */
    private fun handleDownloadError(result: DownloadResult.Error) {
        updateStatus(
            result.documentId,
            DocumentDownloadStatus.Failed(result.error)
        )
    }
    
    /**
     * Process a downloaded file.
     *
     * @param result The download result
     * @return The updated document
     */
    private suspend fun processDownloadedFile(result: DownloadResult.Success): Document? {
        try {
            // Get the document from the repository (in a real app)
            // For now, we'll just create a mock document
            // In reality, you'd want to retrieve the document from your repository
            val documentId = result.documentId
            
            // In a real implementation, you'd use a repository to get the document
            // For now, let's just log a message
            Log.d(TAG, "Processing downloaded file for document $documentId")
            Log.d(TAG, "Download URI: ${result.uri}, Size: ${result.size}")
            
            // Return null since we don't have a real implementation yet
            return null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing downloaded file", e)
            return null
        }
    }
    
    /**
     * Update the status of a download.
     *
     * @param documentId The ID of the document
     * @param status The new status
     */
    private fun updateStatus(documentId: String, status: DocumentDownloadStatus) {
        val currentMap = _downloadStatus.value.toMutableMap()
        currentMap[documentId] = status
        _downloadStatus.value = currentMap
    }
    
    /**
     * Get the status for a specific document download.
     *
     * @param documentId The ID of the document
     * @return Flow emitting the download status for the document
     */
    fun getDownloadStatusForDocument(documentId: String): Flow<DocumentDownloadStatus?> {
        return downloadStatus.map { it[documentId] }
    }
    
    /**
     * Clean up resources when the service is no longer needed.
     */
    fun cleanup() {
        downloadReceiver.unregister(context)
    }
}

/**
 * Represents the status of a document download.
 */
sealed class DocumentDownloadStatus {
    object Pending : DocumentDownloadStatus()
    
    data class InProgress(val progress: Float) : DocumentDownloadStatus() {
        val progressPercent: Int
            get() = (progress * 100).toInt()
    }
    
    data class Paused(val reason: String) : DocumentDownloadStatus()
    
    object Completed : DocumentDownloadStatus()
    
    data class Failed(val reason: String) : DocumentDownloadStatus()
    
    object Canceled : DocumentDownloadStatus()
}
