package com.noobdev.numlexambuddy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.RoomWarnings
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
@RoomCompat
interface DocumentQueryDao {/**
     * Get documents with their chat statistics
     */
    @RewriteQueriesToDropUnusedColumns
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
    @Query("SELECT DISTINCT tags FROM documents WHERE tags IS NOT NULL AND tags != ''")
    suspend fun getRawTags(): List<String>
    
    /**
     * Get all unique tags as flattened list
     */
    suspend fun getAllTags(): List<String> {
        val rawTags = getRawTags()
        val result = mutableSetOf<String>()
        
        for (tagString in rawTags) {
            val tags = tagString.split(",")
            result.addAll(tags)
        }
        
        return result.toList()
    }
    
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
    @ColumnInfo(name = "file_path")
    val filePath: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    val size: Long,
    @ColumnInfo(name = "source_url")
    val sourceUrl: String,
    @ColumnInfo(name = "document_type")
    val documentType: DocumentType,
    val status: DocumentStatus,
    @ColumnInfo(name = "download_date")
    val downloadDate: Date,
    @ColumnInfo(name = "last_accessed")
    val lastAccessed: Date?,
    val subject: String?,
    val semester: Int?,
    val department: String?,
    val summary: String?,
    val tags: List<String>,
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date?,
    val sessionCount: Int,
    val messageCount: Int
)
