package com.noobdev.numlexambuddy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.noobdev.numlexambuddy.model.DocumentSummary
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for document summaries
 */
@Dao
interface DocumentSummaryDao {

    /**
     * Insert a new document summary
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DocumentSummary)
    
    /**
     * Update an existing document summary
     */
    @Update
    suspend fun updateSummary(summary: DocumentSummary)
    
    /**
     * Get summary for a specific document
     */
    @Query("SELECT * FROM document_summaries WHERE documentId = :documentId")
    suspend fun getSummaryById(documentId: String): DocumentSummary?
    
    /**
     * Get summary for a specific document as Flow
     */
    @Query("SELECT * FROM document_summaries WHERE documentId = :documentId")
    fun getSummaryByIdAsFlow(documentId: String): Flow<DocumentSummary?>
    
    /**
     * Delete summary for a document
     */
    @Query("DELETE FROM document_summaries WHERE documentId = :documentId")
    suspend fun deleteSummaryById(documentId: String)
    
    /**
     * Check if summary exists for a document
     */
    @Query("SELECT EXISTS(SELECT 1 FROM document_summaries WHERE documentId = :documentId)")
    suspend fun summaryExists(documentId: String): Boolean
      /**
     * Get all document summaries that were recently generated 
     * (we don't have an isComplete column, using generated_date instead)
     */
    @Query("SELECT * FROM document_summaries WHERE generated_date > :recentDate")
    suspend fun getRecentSummaries(recentDate: Long): List<DocumentSummary>
    
    /**
     * Delete summaries older than a certain date (for cache management)
     */
    @Query("DELETE FROM document_summaries WHERE generated_date < :olderThan AND documentId NOT IN (SELECT id FROM documents WHERE last_accessed > :recentlyUsedDate)")
    suspend fun deleteOldUnusedSummaries(olderThan: Long, recentlyUsedDate: Long): Int
}
