package com.noobdev.numlexambuddy.data.parser

import android.util.Log
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentContent
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manager for document content extraction and processing.
 * This class coordinates parsing of different document types and provides
 * content chunks for use with AI processing.
 */
class DocumentContentManager(private val parserFactory: DocumentParserFactory) {
    companion object {
        private const val TAG = "DocumentContentManager"
        
        // Maximum characters to include in a content chunk
        private const val MAX_CHUNK_SIZE = 4000
        
        // Overlap between chunks to maintain context
        private const val CHUNK_OVERLAP = 500
    }

    /**
     * Extract the content of a document.
     * 
     * @param document The document to extract content from
     * @return The extracted content, or null if extraction failed
     */
    suspend fun extractDocumentContent(document: Document): List<DocumentContent> = withContext(Dispatchers.IO) {
        try {
            val parser = parserFactory.getParserForType(document.mimeType)
            val now = Date()
            if (parser == null) {
                // Return a single error chunk
                return@withContext listOf(
                    DocumentContent(
                        documentId = document.id,
                        chunkIndex = 0,
                        content = "Content extraction not supported for this document type",
                        extractedDate = now
                    )
                )
            }
            val content = parser.parseDocument(document.filePath)
            if (content == null) {
                return@withContext listOf(
                    DocumentContent(
                        documentId = document.id,
                        chunkIndex = 0,
                        content = "",
                        extractedDate = now
                    )
                )
            }
            val chunks = if (content.length > MAX_CHUNK_SIZE) createContentChunks(content) else listOf(content)
            return@withContext chunks.mapIndexed { idx, chunk ->
                DocumentContent(
                    documentId = document.id,
                    chunkIndex = idx,
                    content = chunk,
                    extractedDate = now
                )
            }
        } catch (e: Exception) {
            val now = Date()
            return@withContext listOf(
                DocumentContent(
                    documentId = document.id,
                    chunkIndex = 0,
                    content = "Error extracting content: ${e.message}",
                    extractedDate = now
                )
            )
        }
    }

    /**
     * Create content chunks from a large text.
     * This method breaks the text into overlapping chunks suitable for processing with AI models.
     * 
     * @param content The content to chunk
     * @return A list of content chunks
     */
    private fun createContentChunks(content: String): List<String> {
        val chunks = mutableListOf<String>()
        var startIndex = 0
        
        while (startIndex < content.length) {
            // Calculate end index for this chunk
            val endIndex = minOf(startIndex + MAX_CHUNK_SIZE, content.length)
            
            // Extract the chunk
            val chunk = content.substring(startIndex, endIndex)
            
            // Add the chunk to the list
            chunks.add(chunk)
            
            // Calculate the next start index with overlap
            startIndex = endIndex - CHUNK_OVERLAP
            
            // Make sure we don't get stuck in a loop if we're near the end
            if (startIndex >= content.length - CHUNK_OVERLAP) {
                break
            }
        }
        
        // If we're close to the end but haven't added the final part, add it now
        val lastChunk = chunks.lastOrNull()
        if (lastChunk != null && !lastChunk.endsWith(content.takeLast(100))) {
            chunks.add(content.takeLast(minOf(MAX_CHUNK_SIZE, content.length)))
        }
        
        return chunks
    }

    /**
     * Count the number of words in a text.
     * 
     * @param text The text to count words in
     * @return The number of words
     */
    private fun countWords(text: String): Int {
        return text.split("\\s+".toRegex()).count { it.isNotBlank() }
    }

    /**
     * Clean and format extracted content.
     * This removes excessive whitespace, normalizes line breaks, etc.
     * 
     * @param content The content to clean
     * @return The cleaned content
     */
    fun cleanContent(content: String): String {
        return content
            // Replace multiple newlines with double newlines
            .replace(Regex("\n{3,}"), "\n\n")
            // Replace multiple spaces with a single space
            .replace(Regex(" {2,}"), " ")
            // Trim leading/trailing whitespace
            .trim()
    }
}
