package com.noobdev.numlexambuddy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentStatus
import com.noobdev.numlexambuddy.model.DocumentType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Additional DAO for more complex document-related queries
 */
@Dao
interface DocumentQueryDao {

    /**
     * Get documents with their chat statistics
     */
    @Query("""
        SELECT d.*, COUNT(DISTINCT s.id) as sessionCount, COUNT(DISTINCT m.id) as messageCount
        FROM documents d
        LEFT JOIN chat_sessions s ON d.id = s.document_id
        LEFT JOIN chat_messages m ON d.id = m.document_id
        GROUP BY d.id
    """)
    suspend fun getDocumentsWithChatStats(): List<DocumentWithChatStats>
    
    /**
     * Advanced search across document properties
     */
    @Query("""
        SELECT * FROM documents 
        WHERE (title LIKE '%' || :query || '%') 
        OR (summary LIKE '%' || :query || '%')
        OR (subject LIKE '%' || :query || '%')
        OR (department LIKE '%' || :query || '%')
        OR (tags LIKE '%' || :query || '%')
    """)
    suspend fun advancedSearch(query: String): List<Document>
    
    /**
     * Filter documents by multiple criteria
     */
    @Query("""
        SELECT * FROM documents
        WHERE (:subject IS NULL OR subject = :subject)
        AND (:semester IS NULL OR semester = :semester)
        AND (:department IS NULL OR department = :department)
        AND (:documentType IS NULL OR document_type = :documentType)
        AND (:status IS NULL OR status = :status)
    """)
    suspend fun filterDocuments(
        subject: String? = null,
        semester: Int? = null,
        department: String? = null,
        documentType: DocumentType? = null,
        status: DocumentStatus? = null
    ): List<Document>
    
    /**
     * Get documents added in date range
     */
    @Query("SELECT * FROM documents WHERE download_date BETWEEN :startDate AND :endDate")
    suspend fun getDocumentsInDateRange(startDate: Date, endDate: Date): List<Document>
    
    /**
     * Get documents accessed in date range
     */
    @Query("SELECT * FROM documents WHERE last_accessed BETWEEN :startDate AND :endDate")
    suspend fun getDocumentsAccessedInDateRange(startDate: Date, endDate: Date): List<Document>
    
    /**
     * Get subjects available in documents
     */
    @Query("SELECT DISTINCT subject FROM documents WHERE subject IS NOT NULL")
    suspend fun getAllSubjects(): List<String>
    
    /**
     * Get departments available in documents
     */
    @Query("SELECT DISTINCT department FROM documents WHERE department IS NOT NULL")
    suspend fun getAllDepartments(): List<String>
    
    /**
     * Get semesters available in documents
     */
    @Query("SELECT DISTINCT semester FROM documents WHERE semester IS NOT NULL")
    suspend fun getAllSemesters(): List<Int>
    
    /**
     * Get all unique tags across all documents
     */
    @Transaction
    @Query("SELECT tags FROM documents")
    suspend fun getAllTags(): List<List<String>>
    
    /**
     * Get documents that require summarization (have null summary)
     */
    @Query("SELECT * FROM documents WHERE summary IS NULL AND status = :status")
    suspend fun getDocumentsRequiringSummary(status: DocumentStatus = DocumentStatus.COMPLETE): List<Document>
}

/**
 * Data class for document with chat statistics
 */
data class DocumentWithChatStats(
    val id: String,
    val title: String,
    val filePath: String,
    val mimeType: String,
    val size: Long,
    val sourceUrl: String,
    val documentType: DocumentType,
    val status: DocumentStatus,
    val downloadDate: Date,
    val lastAccessed: Date?,
    val subject: String?,
    val semester: Int?,
    val department: String?,
    val summary: String?,
    val tags: List<String>,
    val createdAt: Date,
    val updatedAt: Date?,
    val sessionCount: Int,
    val messageCount: Int
)
