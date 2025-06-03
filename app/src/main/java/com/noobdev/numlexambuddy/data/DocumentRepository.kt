package com.noobdev.numlexambuddy.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.noobdev.numlexambuddy.data.dao.DocumentDao
import com.noobdev.numlexambuddy.data.dao.DocumentQueryDao
import com.noobdev.numlexambuddy.data.download.DocumentDownloadService
import com.noobdev.numlexambuddy.data.download.DocumentDownloadStatus
import com.noobdev.numlexambuddy.data.storage.DocumentStorageManager
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentContent
import com.noobdev.numlexambuddy.model.DocumentStatus
import com.noobdev.numlexambuddy.model.DocumentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

/**
 * Repository for managing documents.
 * Handles database operations, document storage, and download management.
 */
class DocumentRepository(
    private val documentDao: DocumentDao,
    private val documentQueryDao: DocumentQueryDao,
    private val storageManager: DocumentStorageManager,
    private val downloadService: DocumentDownloadService,
    private val documentContentHelper: DocumentContentHelper
) {
    companion object {
        private const val TAG = "DocumentRepository"
    }

    /**
     * Get all documents.
     */
    fun getAllDocuments(): Flow<List<Document>> {
        return flow { emit(documentDao.getAllDocuments()) }
    }

    /**
     * Get a document by ID.
     */
    suspend fun getDocumentById(id: String): Document? {
        return documentDao.getDocumentById(id)
    }

    /**
     * Get documents by status.
     */
    fun getDocumentsByStatus(status: DocumentStatus): Flow<List<Document>> {
        return flow { emit(documentDao.getDocumentsByStatus(status)) }
    }

    /**
     * Get documents by type.
     */
    fun getDocumentsByType(documentType: DocumentType): Flow<List<Document>> {
        return flow { emit(documentDao.getDocumentsByType(documentType)) }
    }

    /**
     * Search documents by title.
     */
    fun searchDocumentsByTitle(query: String): Flow<List<Document>> {
        return flow { emit(documentDao.searchDocumentsByTitle("%$query%")) }
    }

    /**
     * Get documents for a subject.
     */
    fun getDocumentsForSubject(subject: String): Flow<List<Document>> {
        return flow { emit(documentDao.getDocumentsBySubject(subject)) }
    }

    /**
     * Get documents for a semester.
     */
    fun getDocumentsForSemester(semester: Int): Flow<List<Document>> {
        return flow { emit(documentDao.getDocumentsBySemester(semester)) }
    }

    /**
     * Get documents for a department.
     */
    fun getDocumentsForDepartment(department: String): Flow<List<Document>> {
        return flow { emit(documentDao.getDocumentsByDepartment(department)) }
    }

    /**
     * Get documents that contain any of the specified tags.
     */
    fun getDocumentsWithAnyTags(tags: List<String>): Flow<List<Document>> {
        return documentQueryDao.getDocumentsWithAnyTags(tags)
    }
    
    /**
     * Get documents that contain all of the specified tags.
     */
    fun getDocumentsWithAllTags(tags: List<String>): Flow<List<Document>> {
        return documentQueryDao.getDocumentsWithAllTags(tags)
    }

    /**
     * Add a document to the repository.
     * This will save the document to the database.
     */
    suspend fun addDocument(document: Document): Long = withContext(Dispatchers.IO) {
        return@withContext documentDao.insertDocument(document)
    }

    /**
     * Update a document in the repository.
     * This will update the document in the database.
     */
    suspend fun updateDocument(document: Document): Int = withContext(Dispatchers.IO) {
        return@withContext documentDao.updateDocument(document)
    }

    /**
     * Delete a document from the repository.
     * This will delete the document from the database and optionally delete the file.
     */
    suspend fun deleteDocument(document: Document, deleteFile: Boolean = true): Int = withContext(Dispatchers.IO) {
        if (deleteFile) {
            try {
                // Delete the file
                val file = File(document.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting document file: ${e.message}", e)
            }
        }
        
        // Delete from database
        return@withContext documentDao.deleteDocument(document)
    }

    suspend fun getDocumentContent(documentId: String): List<DocumentContent> {
        val document = getDocumentById(documentId)
        return if (document != null) {
            documentContentHelper.getDocumentContent(document)
        } else {
            emptyList()
        }
    }

    /**
     * Mark a document as deleted without removing it from the database.
     * This is useful for "soft deletes" where you want to keep a record but hide it from users.
     */
    suspend fun markDocumentAsDeleted(document: Document): Int = withContext(Dispatchers.IO) {
        val updatedDocument = document.copy(status = DocumentStatus.DELETED)
        return@withContext documentDao.updateDocument(updatedDocument)
    }

    /**
     * Update document metadata.
     */
    suspend fun updateDocumentMetadata(
        document: Document,
        newTitle: String? = null,
        newSubject: String? = null,
        newSemester: Int? = null,
        newDepartment: String? = null,
        newTags: List<String>? = null
    ): Int = withContext(Dispatchers.IO) {
        val updatedDocument = document.copy(
            title = newTitle ?: document.title,
            subject = newSubject ?: document.subject,
            semester = newSemester ?: document.semester,
            department = newDepartment ?: document.department,
            tags = newTags ?: document.tags
        )
        
        return@withContext documentDao.updateDocument(updatedDocument)
    }

    /**
     * Import a document from a URI.
     * This will save the file to storage and add it to the database.
     */
    suspend fun importDocumentFromUri(
        uri: Uri,
        fileName: String,
        mimeType: String,
        sourceUrl: String,
        subject: String? = null,
        semester: Int? = null,
        department: String? = null,
        tags: List<String> = emptyList()
    ): Document? = withContext(Dispatchers.IO) {
        // Save the file to storage
        val document = storageManager.saveDocumentFromUri(
            sourceUri = uri,
            fileName = fileName,
            mimeType = mimeType,
            sourceUrl = sourceUrl,
            subject = subject,
            semester = semester,
            department = department,
            tags = tags
        )
        
        if (document != null) {
            // Add to database
            documentDao.insertDocument(document)
            return@withContext document
        }
        
        return@withContext null
    }

    /**
     * Download a document from a URL.
     * This will create a pending document, start the download, and update the document when complete.
     */
    suspend fun downloadDocument(
        sourceUrl: String,
        fileName: String,
        mimeType: String,
        estimatedSize: Long = 0,
        subject: String? = null,
        semester: Int? = null,
        department: String? = null,
        tags: List<String> = emptyList()
    ): Document? = withContext(Dispatchers.IO) {
        try {
            // Create a pending document
            val pendingDocument = storageManager.createPendingDocument(
                fileName = fileName,
                mimeType = mimeType,
                sourceUrl = sourceUrl,
                estimatedSize = estimatedSize,
                subject = subject,
                semester = semester,
                department = department,
                tags = tags
            )
            
            // Add to database
            val id = documentDao.insertDocument(pendingDocument)
            if (id > 0) {
                // Start the download
                val updatedDocument = downloadService.startDownload(pendingDocument)
                
                // Return the document with updated status
                return@withContext updatedDocument
            }
            
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading document: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Get the download status for a document.
     */
    fun getDownloadStatus(documentId: String): Flow<DocumentDownloadStatus?> {
        return downloadService.getDownloadStatusForDocument(documentId)
    }

    /**
     * Cancel a document download.
     */
    fun cancelDownload(documentId: String): Boolean {
        return downloadService.cancelDownload(documentId)
    }

    /**
     * Update a document's last accessed timestamp.
     */
    suspend fun updateLastAccessed(documentId: String): Int = withContext(Dispatchers.IO) {
        val document = documentDao.getDocumentById(documentId) ?: return@withContext 0
        
        val updatedDocument = document.copy(
            lastAccessed = Date()
        )
        
        return@withContext documentDao.updateDocument(updatedDocument)
    }

    /**
     * Update a document's summary.
     */
    suspend fun updateDocumentSummary(documentId: String, summary: String): Int = withContext(Dispatchers.IO) {
        val document = documentDao.getDocumentById(documentId) ?: return@withContext 0
        
        val updatedDocument = document.copy(
            summary = summary
        )
        
        return@withContext documentDao.updateDocument(updatedDocument)
    }

    /**
     * Get documents sorted by last accessed date.
     */
    fun getRecentDocuments(limit: Int = 10): Flow<List<Document>> {
        return flow { emit(documentDao.getRecentDocuments(limit)) }
    }

    /**
     * Clean up resources when the repository is no longer needed.
     */
    fun cleanup() {
        downloadService.cleanup()
    }
}
