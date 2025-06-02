package com.noobdev.numlexambuddy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentStatus
import com.noobdev.numlexambuddy.model.DocumentType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Document entity.
 * Provides methods to interact with the documents table in the database.
 */
@Dao
@RoomCompat
interface DocumentDao {

    // Basic CRUD Operations
    
    /**
     * Insert a new document into the database
     * If there's a conflict (same document ID), replace the existing document
     */    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long
    
    /**
     * Insert multiple documents at once
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<Document>): List<Long>
    
    /**
     * Update an existing document
     */
    @Update
    suspend fun updateDocument(document: Document): Int
    
    /**
     * Delete a document from the database
     * Note: This does not delete the actual file, only the database record
     */
    @Delete
    suspend fun deleteDocument(document: Document): Int
    
    /**
     * Delete a document by its ID
     */
    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocumentById(documentId: String): Int
    
    // Document Retrieval Queries
    
    /**
     * Get a document by its ID
     */
    @Query("SELECT * FROM documents WHERE id = :documentId")
    suspend fun getDocumentById(documentId: String): Document?
    
    /**
     * Get a document by its ID as LiveData for observation
     */
    @Query("SELECT * FROM documents WHERE id = :documentId")
    fun observeDocumentById(documentId: String): LiveData<Document?>
    
    /**
     * Get all documents in the database
     */
    @Query("SELECT * FROM documents")
    suspend fun getAllDocuments(): List<Document>
    
    /**
     * Get all documents as Flow for reactive programming
     */
    @Query("SELECT * FROM documents")
    fun observeAllDocuments(): Flow<List<Document>>
    
    /**
     * Get all documents as LiveData for observation
     */
    @Query("SELECT * FROM documents")
    fun getAllDocumentsLiveData(): LiveData<List<Document>>
    
    // Filtering and Searching Queries
    
    /**
     * Get documents by their status
     */
    @Query("SELECT * FROM documents WHERE status = :status")
    suspend fun getDocumentsByStatus(status: DocumentStatus): List<Document>
    
    /**
     * Get documents by their type
     */
    @Query("SELECT * FROM documents WHERE document_type = :documentType")
    suspend fun getDocumentsByType(documentType: DocumentType): List<Document>
    
    /**
     * Get documents by subject
     */
    @Query("SELECT * FROM documents WHERE subject = :subject")
    suspend fun getDocumentsBySubject(subject: String): List<Document>
    
    /**
     * Get documents by semester
     */
    @Query("SELECT * FROM documents WHERE semester = :semester")
    suspend fun getDocumentsBySemester(semester: Int): List<Document>
    
    /**
     * Get documents by department
     */
    @Query("SELECT * FROM documents WHERE department = :department")
    suspend fun getDocumentsByDepartment(department: String): List<Document>
    
    /**
     * Search documents by title
     */
    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%'")
    suspend fun searchDocumentsByTitle(query: String): List<Document>
    
    /**
     * Full text search in title and summary
     */
    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%'")
    suspend fun searchDocuments(query: String): List<Document>
    
    /**
     * Get recent documents ordered by last accessed date
     */
    @Query("SELECT * FROM documents ORDER BY last_accessed DESC LIMIT :limit")
    suspend fun getRecentDocuments(limit: Int = 10): List<Document>
    
    /**
     * Get recently added documents ordered by download date
     */
    @Query("SELECT * FROM documents ORDER BY download_date DESC LIMIT :limit")
    suspend fun getRecentlyAddedDocuments(limit: Int = 10): List<Document>
    
    // Additional utility queries
    
    /**
     * Update document status
     */
    @Query("UPDATE documents SET status = :newStatus WHERE id = :documentId")
    suspend fun updateDocumentStatus(documentId: String, newStatus: DocumentStatus): Int
    
    /**
     * Update last accessed timestamp
     */
    @Query("UPDATE documents SET last_accessed = :timestamp, updated_at = :timestamp WHERE id = :documentId")
    suspend fun updateLastAccessed(documentId: String, timestamp: Long): Int
    
    /**
     * Count documents by type
     */
    @Query("SELECT COUNT(*) FROM documents WHERE document_type = :documentType")
    suspend fun countDocumentsByType(documentType: DocumentType): Int
    
    /**
     * Get total number of documents
     */
    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int
    
    /**
     * Get documents with tags containing the specified tag
     * Note: This uses the StringListConverter to handle the List<String> type
     */
    @Query("SELECT * FROM documents WHERE tags LIKE '%' || :tag || '%'")
    suspend fun getDocumentsWithTag(tag: String): List<Document>
}
