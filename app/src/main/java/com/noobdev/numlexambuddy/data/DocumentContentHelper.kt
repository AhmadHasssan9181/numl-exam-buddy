package com.noobdev.numlexambuddy.data

import com.noobdev.numlexambuddy.model.DocumentContent
import java.util.Date
import kotlin.math.min

/**
 * Utility for chunking document content for efficient AI processing
 */
object DocumentContentHelper {
    
    // Default size for text chunks (approx. 1000 tokens)
    private const val DEFAULT_CHUNK_SIZE = 4000
    
    // Default overlap between chunks to maintain context
    private const val DEFAULT_OVERLAP = 500
    
    /**
     * Splits document text into overlapping chunks for processing
     * 
     * @param documentId The document ID
     * @param fullText The complete text of the document
     * @param chunkSize Size of each chunk in characters (default 4000)
     * @param overlap Overlap between chunks in characters (default 500)
     * @return List of DocumentContent objects containing the chunks
     */
    fun chunkDocumentText(
        documentId: String, 
        fullText: String,
        chunkSize: Int = DEFAULT_CHUNK_SIZE,
        overlap: Int = DEFAULT_OVERLAP
    ): List<DocumentContent> {
        if (fullText.isEmpty()) return emptyList()
        if (fullText.length <= chunkSize) {
            // If text is smaller than chunk size, just return as one chunk
            return listOf(
                DocumentContent(
                    documentId = documentId,
                    chunkIndex = 0,
                    content = fullText,
                    extractedDate = Date()
                )
            )
        }
        
        val chunks = mutableListOf<DocumentContent>()
        var startIndex = 0
        var chunkIndex = 0
        
        while (startIndex < fullText.length) {
            val endIndex = min(startIndex + chunkSize, fullText.length)
            
            // Try to find a good breaking point (newline or period)
            var breakPoint = findBreakPoint(fullText, endIndex)
            
            // If we couldn't find a good break point within a reasonable range, just use endIndex
            if (breakPoint < endIndex - 100) {
                breakPoint = endIndex
            }
            
            // Extract the chunk
            val chunkText = fullText.substring(startIndex, breakPoint)
            
            chunks.add(
                DocumentContent(
                    documentId = documentId,
                    chunkIndex = chunkIndex,
                    content = chunkText,
                    extractedDate = Date()
                )
            )
            
            // Update for next chunk, with overlap
            startIndex = breakPoint - overlap
            if (startIndex < 0) startIndex = 0
            chunkIndex++
            
            // Safety check for endless loops
            if (startIndex >= fullText.length || chunkIndex > 1000) break
        }
        
        return chunks
    }
    
    /**
     * Finds a suitable break point (period, paragraph, etc.) near the endIndex
     */
    private fun findBreakPoint(text: String, endIndex: Int): Int {
        // First look for paragraph breaks
        for (i in endIndex downTo endIndex - 100) {
            if (i < 0 || i >= text.length) continue
            
            // Check for double newlines (paragraph)
            if (i > 0 && text[i] == '\n' && text[i-1] == '\n') {
                return i + 1
            }
        }
        
        // Then look for single newlines
        for (i in endIndex downTo endIndex - 100) {
            if (i < 0 || i >= text.length) continue
            
            if (text[i] == '\n') {
                return i + 1
            }
        }
        
        // Then look for sentence breaks (period + space)
        for (i in endIndex downTo endIndex - 100) {
            if (i < 0 || i + 1 >= text.length) continue
            
            if (text[i] == '.' && text[i+1] == ' ') {
                return i + 2
            }
        }
        
        // If no natural breaks found, just return the endIndex
        return endIndex
    }
    
    /**
     * Extracts potential section titles from text
     */
    fun extractSectionTitles(text: String): List<String> {
        val titles = mutableListOf<String>()
        val lines = text.split("\n")
        
        for (line in lines) {
            val trimmed = line.trim()
            
            // Potential title characteristics:
            // 1. Short line (< 80 chars)
            // 2. No punctuation except . , : -
            // 3. Ends with colon or all caps or starts with number.
            if (trimmed.length < 80 && trimmed.isNotEmpty() &&
                (trimmed.endsWith(":") || 
                 trimmed.uppercase() == trimmed ||
                 trimmed.matches(Regex("^\\d+\\.?\\s+.*")) ||
                 trimmed.matches(Regex("^[A-Z][a-z]+\\s+\\d+.*")) // "Chapter 1" format
                )
            ) {
                titles.add(trimmed)
            }
        }
        
        return titles
    }
}
