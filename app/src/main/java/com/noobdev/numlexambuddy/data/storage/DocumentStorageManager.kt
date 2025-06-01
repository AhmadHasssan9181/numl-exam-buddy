package com.noobdev.numlexambuddy.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentStatus
import com.noobdev.numlexambuddy.model.DocumentType
import com.noobdev.numlexambuddy.utils.FileStorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.Date
import java.util.UUID

/**
 * Manager class for document storage operations.
 * Handles saving, retrieving, and deleting documents from storage.
 */
class DocumentStorageManager(private val context: Context) {

    private val tag = "DocumentStorageManager"

    /**
     * Saves a document from a URI to internal storage.
     * @param sourceUri The URI of the source document.
     * @param fileName The name of the document.
     * @param mimeType The MIME type of the document.
     * @param sourceUrl The source URL of the document.
     * @return A Document object if the operation is successful, null otherwise.
     */
    suspend fun saveDocumentFromUri(
        sourceUri: Uri,
        fileName: String,
        mimeType: String,
        sourceUrl: String,
        subject: String? = null,
        semester: Int? = null,
        department: String? = null,
        tags: List<String> = emptyList()
    ): Document? = withContext(Dispatchers.IO) {
        try {
            val documentType = FileStorageUtils.detectDocumentType(mimeType)
            val fileSize = FileStorageUtils.getFileSizeFromUri(context, sourceUri)
            
            // Check if there's enough storage space
            if (!FileStorageUtils.isStorageAvailable(context, fileSize)) {
                Log.e(tag, "Not enough storage space for document: $fileName")
                return@withContext null
            }
            
            // Save the file to internal storage
            val filePath = FileStorageUtils.saveFileFromUri(context, sourceUri, fileName, documentType)
            if (filePath == null) {
                Log.e(tag, "Failed to save document: $fileName")
                return@withContext null
            }
            
            // Create and return the Document object
            val documentId = UUID.randomUUID().toString()
            return@withContext Document(
                id = documentId,
                title = fileName,
                filePath = filePath,
                mimeType = mimeType,
                size = fileSize,
                sourceUrl = sourceUrl,
                documentType = documentType,
                status = DocumentStatus.COMPLETE,
                downloadDate = Date(),
                lastAccessed = Date(),
                subject = subject,
                semester = semester,
                department = department,
                tags = tags
            )
        } catch (e: Exception) {
            Log.e(tag, "Error saving document from URI", e)
            return@withContext null
        }
    }
    
    /**
     * Creates a pending document entry before downloading.
     * @param fileName The name of the document.
     * @param mimeType The MIME type of the document.
     * @param sourceUrl The source URL of the document.
     * @return A Document object with PENDING status.
     */
    fun createPendingDocument(
        fileName: String,
        mimeType: String,
        sourceUrl: String,
        estimatedSize: Long = 0L,
        subject: String? = null,
        semester: Int? = null,
        department: String? = null,
        tags: List<String> = emptyList()
    ): Document {
        val documentType = FileStorageUtils.detectDocumentType(mimeType)
        val documentId = UUID.randomUUID().toString()
        val filePath = FileStorageUtils.generateDocumentFilePath(context, fileName, documentType)
        
        return Document(
            id = documentId,
            title = fileName,
            filePath = filePath,
            mimeType = mimeType,
            size = estimatedSize,
            sourceUrl = sourceUrl,
            documentType = documentType,
            status = DocumentStatus.PENDING,
            downloadDate = Date(),
            lastAccessed = null,
            subject = subject,
            semester = semester,
            department = department,
            tags = tags
        )
    }
    
    /**
     * Updates a document's status to DOWNLOADING.
     * @param document The document to update.
     * @return The updated document.
     */
    fun updateDocumentToDownloading(document: Document): Document {
        return document.copy(status = DocumentStatus.DOWNLOADING)
    }
    
    /**
     * Finalizes a document after successful download.
     * @param document The document to finalize.
     * @param actualSize The actual size of the downloaded file.
     * @return The finalized document.
     */
    fun finalizeDocument(document: Document, actualSize: Long): Document {
        return document.copy(
            status = DocumentStatus.COMPLETE,
            size = actualSize,
            lastAccessed = Date()
        )
    }
    
    /**
     * Marks a document as failed.
     * @param document The document to mark as failed.
     * @return The updated document.
     */
    fun markDocumentAsFailed(document: Document): Document {
        return document.copy(status = DocumentStatus.FAILED)
    }

    /**
     * Gets a document file from storage.
     * @param filePath The path of the document file.
     * @return The File object, or null if the file doesn't exist.
     */
    fun getDocumentFile(filePath: String): File? {
        val file = File(filePath)
        return if (file.exists() && file.isFile) file else null
    }

    /**
     * Gets an input stream for a document.
     * @param filePath The path of the document file.
     * @return An InputStream for the document, or null if the file doesn't exist.
     */
    fun getDocumentInputStream(filePath: String): InputStream? {
        val file = getDocumentFile(filePath)
        return file?.inputStream()
    }

    /**
     * Deletes a document from storage.
     * @param document The document to delete.
     * @return true if the document was deleted, false otherwise.
     */
    suspend fun deleteDocument(document: Document): Boolean = withContext(Dispatchers.IO) {
        try {
            // Delete the file
            val result = FileStorageUtils.deleteFile(document.filePath)
            if (result) {
                Log.d(tag, "Document deleted: ${document.title}")
            } else {
                Log.e(tag, "Failed to delete document: ${document.title}")
            }
            result
        } catch (e: Exception) {
            Log.e(tag, "Error deleting document", e)
            false
        }
    }

    /**
     * Updates the last accessed timestamp for a document.
     * @param document The document to update.
     * @return The updated document.
     */
    fun updateLastAccessed(document: Document): Document {
        val now = Date()
        return document.copy(
            lastAccessed = now,
            updatedAt = now
        )
    }

    /**
     * Exports a document to external storage.
     * @param document The document to export.
     * @param destinationUri The destination URI.
     * @return true if the document was exported, false otherwise.
     */
    suspend fun exportDocument(document: Document, destinationUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(document.filePath)
            if (!sourceFile.exists()) {
                Log.e(tag, "Source document doesn't exist: ${document.filePath}")
                return@withContext false
            }
            
            // Copy the file to the destination
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
                return@withContext true
            }
            
            Log.e(tag, "Failed to open output stream for: $destinationUri")
            false
        } catch (e: IOException) {
            Log.e(tag, "Error exporting document", e)
            false
        }
    }

    /**
     * Gets a shareable URI for a document.
     * @param document The document.
     * @return A content URI for the document.
     */
    fun getShareableDocumentUri(document: Document): Uri {
        return FileStorageUtils.getShareableUri(context, document.filePath)
    }

    /**
     * Cleans up temporary files used during document operations.
     */
    fun cleanupTempFiles() {
        try {
            FileStorageUtils.cleanTempDirectory(context)
        } catch (e: Exception) {
            Log.e(tag, "Error cleaning temp directory", e)
        }
    }
}
