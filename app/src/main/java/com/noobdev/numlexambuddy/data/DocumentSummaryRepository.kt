package com.noobdev.numlexambuddy.data

import android.util.Log
import com.noobdev.numlexambuddy.data.dao.DocumentSummaryDao
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentContent
import com.noobdev.numlexambuddy.model.DocumentSummary
import com.noobdev.numlexambuddy.service.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Repository for managing document summaries
 */
class DocumentSummaryRepository(
    private val documentSummaryDao: DocumentSummaryDao,
    private val geminiService: GeminiService,
    private val documentContentHelper: DocumentContentHelper
) {
    companion object {
        private const val TAG = "DocumentSummaryRepo"
        
        // TTL for cached summaries (7 days)
        private val SUMMARY_CACHE_TTL = TimeUnit.DAYS.toMillis(7)
        
        // Recently used threshold (2 days)
        private val RECENTLY_USED_THRESHOLD = TimeUnit.DAYS.toMillis(2)
    }
    
    /**
     * Gets a document summary, generating it if not available in cache
     */
    suspend fun getDocumentSummary(document: Document): Result<String> {
        // Check for cached summary
        val cachedSummary = documentSummaryDao.getSummaryById(document.id)
        
        // Return cached summary if still valid
        if (cachedSummary != null && isCacheValid(cachedSummary.generatedDate)) {
            return Result.success(cachedSummary.summaryText)
        }
        
        // If cache expired or doesn't exist, generate new summary
        return try {
            // Get document content
            val documentContent = documentContentHelper.getDocumentContent(document)
            if (documentContent.isEmpty()) {
                return Result.failure(Exception("No content available for document"))
            }
            
            // Generate summary using Gemini
            val summaryResult = geminiService.generateSummary(document, documentContent)
            
            if (summaryResult.isSuccess) {
                val summary = summaryResult.getOrThrow()
                val now = Date()
                
                // Save to cache
                val documentSummary = DocumentSummary(
                    documentId = document.id,
                    summaryText = summary,
                    generatedDate = now,
                    lastUpdated = now
                )
                
                documentSummaryDao.insertSummary(documentSummary)
                
                // Clean up old cache entries periodically
                cleanupOldCacheEntries()
                
                Result.success(summary)
            } else {
                Result.failure(summaryResult.exceptionOrNull() ?: Exception("Unknown error generating summary"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting document summary", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets a document summary as a Flow, useful for UI updates
     */
    fun getDocumentSummaryAsFlow(documentId: String): Flow<String?> {
        return documentSummaryDao.getSummaryByIdAsFlow(documentId)
            .map { it?.summaryText }
    }
    
    /**
     * Checks if cached summary is still valid
     */
    private fun isCacheValid(generatedAt: Date): Boolean {
        val currentTime = System.currentTimeMillis()
        val cacheTime = generatedAt.time
        
        return (currentTime - cacheTime) < SUMMARY_CACHE_TTL
    }
    
    /**
     * Removes old cache entries to free up space
     */
    private suspend fun cleanupOldCacheEntries() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7) // Older than 7 days
        val olderThan = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, 5) // Used in last 2 days
        val recentlyUsedDate = calendar.timeInMillis
        
        val deletedCount = documentSummaryDao.deleteOldUnusedSummaries(olderThan, recentlyUsedDate)
        if (deletedCount > 0) {
            Log.d(TAG, "Cleaned up $deletedCount old summary cache entries")
        }
    }
    
    /**
     * Invalidates a cached summary for a document
     */
    suspend fun invalidateSummary(documentId: String) {
        documentSummaryDao.deleteSummaryById(documentId)
    }
}
