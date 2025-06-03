package com.noobdev.numlexambuddy.service

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.content
import com.noobdev.numlexambuddy.model.ChatMessage
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentContent
import com.noobdev.numlexambuddy.model.MessageRole
import com.noobdev.numlexambuddy.utils.GeminiErrorHandler
import com.noobdev.numlexambuddy.utils.PromptGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Implementation of GeminiService using Google's Generative AI client.
 */
class GeminiServiceImpl(
    private val context: Context,
    private val errorHandler: GeminiErrorHandler,
    private val promptGenerator: PromptGenerator
) : GeminiService {
      companion object {
        private const val TAG = "GeminiService"
          private const val API_KEY = com.noobdev.numlexambuddy.BuildConfig.GEMINI_API_KEY
          private const val MODEL_NAME = "gemini-2.0-flash" // Updated to newer Gemini model
        
        // Token limits
        private const val MAX_PROMPT_TOKENS = 30000
        private const val MAX_DOCUMENT_TOKENS = 15000
        private const val MAX_HISTORY_TOKENS = 5000
        
        // Helper constants
        private const val CHARS_PER_TOKEN = 4  // Approximation
    }
    
    // Create safety settings for content filtering
    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE)
    )
    
    // Initialize the Gemini model
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY,
            safetySettings = safetySettings
        )
    }
    
    override suspend fun sendMessage(
        query: String,
        document: Document,
        documentContent: List<DocumentContent>?,
        chatHistory: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Convert chat history to the format expected by Gemini API
            val history = convertChatHistoryToContent(chatHistory)
            
            // Select relevant document chunks based on the query
            val relevantContent = selectRelevantContent(query, documentContent)
            
            // Create the prompt with document context
            val prompt = createDocumentContextPrompt(query, document, relevantContent)
            // Generate response from Gemini

            // Generate response from Gemini
            val response = generativeModel.generateContent(
                *(history + content(role = "user") { TextPart(prompt) }).toTypedArray()
            )

            val responseText = response.text
            if (responseText != null) {
                return@withContext Result.success(responseText)
            } else {
                return@withContext Result.failure(IOException("Empty response from Gemini"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message to Gemini", e)
            return@withContext Result.failure(e)
        }
    }
      override suspend fun generateSummary(
        document: Document,
        documentContent: List<DocumentContent>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Check if document is too large for single pass processing
            val isLargeDocument = documentContent.sumOf { it.content.length } > MAX_DOCUMENT_TOKENS * CHARS_PER_TOKEN
            
            if (!isLargeDocument) {
                // For smaller documents, generate summary in a single pass
                val documentText = extractDocumentText(documentContent, maxTokens = MAX_DOCUMENT_TOKENS)
                
                val prompt = """
                    Please provide a concise summary of the following document titled "${document.title}". 
                    Focus on the main points, key arguments, and important conclusions.
                    
                    DOCUMENT CONTENT:
                    $documentText
                    
                    SUMMARY:
                """.trimIndent()
                
                val response = generativeModel.generateContent(prompt)
                
                if (response.text != null) {
                    return@withContext Result.success(response.text!!)
                } else {
                    return@withContext Result.failure(IOException("Empty response from Gemini"))
                }
            } else {
                // For large documents, process in chunks and combine summaries
                val chunks = splitIntoChunks(documentContent, MAX_DOCUMENT_TOKENS * CHARS_PER_TOKEN / 4)
                val chunkSummaries = mutableListOf<String>()
                
                // Generate summary for each chunk
                for (chunk in chunks) {
                    val chunkText = extractDocumentText(chunk, maxTokens = MAX_DOCUMENT_TOKENS)
                    
                    val prompt = """
                        Please provide a brief summary of this section from a document titled "${document.title}".
                        Focus on key points only.
                        
                        SECTION CONTENT:
                        $chunkText
                        
                        SECTION SUMMARY:
                    """.trimIndent()
                    
                    try {
                        val response = generativeModel.generateContent(prompt)
                        if (response.text != null && response.text!!.isNotBlank()) {
                            chunkSummaries.add(response.text!!)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error generating chunk summary", e)
                        // Continue with other chunks if one fails
                    }
                }
                
                // If we got chunk summaries, create a combined summary
                if (chunkSummaries.isNotEmpty()) {
                    val combinedChunkSummaries = chunkSummaries.joinToString("\n\n")
                    
                    val finalPrompt = """
                        Below are summaries of different sections of a document titled "${document.title}".
                        Please create a coherent, concise final summary of the entire document based on these section summaries.
                        
                        SECTION SUMMARIES:
                        $combinedChunkSummaries
                        
                        FINAL DOCUMENT SUMMARY:
                    """.trimIndent()
                    
                    val finalResponse = generativeModel.generateContent(finalPrompt)
                    
                    if (finalResponse.text != null) {
                        return@withContext Result.success(finalResponse.text!!)
                    } else {
                        // If final combination fails, return the individual summaries
                        return@withContext Result.success("Document Summary:\n\n${chunkSummaries.joinToString("\n\n")}")
                    }
                } else {
                    return@withContext Result.failure(IOException("Failed to generate summary for document chunks"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating summary with Gemini", e)
            return@withContext Result.failure(e)
        }
    }
    
    override suspend fun answerQuestion(
        question: String,
        document: Document,
        documentContent: List<DocumentContent>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Select relevant document chunks based on the question
            val relevantContent = selectRelevantContent(question, documentContent)
            
            // Create the prompt with document context
            val prompt = createQuestionAnswerPrompt(question, document, relevantContent)
            
            // Generate response from Gemini
            val response = generativeModel.generateContent(prompt)
            
            if (response.text != null) {
                return@withContext Result.success(response.text!!)
            } else {
                return@withContext Result.failure(IOException("Empty response from Gemini"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error answering question with Gemini", e)
            return@withContext Result.failure(e)
        }
    }
    
    override suspend fun extractInformation(
        document: Document,
        documentContent: List<DocumentContent>,
        extractionType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val documentText = extractDocumentText(documentContent, maxTokens = MAX_DOCUMENT_TOKENS)
            
            val prompt = """
                Please extract the following information from this document titled "${document.title}": $extractionType
                
                DOCUMENT CONTENT:
                $documentText
                
                $extractionType:
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            
            if (response.text != null) {
                return@withContext Result.success(response.text!!)
            } else {
                return@withContext Result.failure(IOException("Empty response from Gemini"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting information with Gemini", e)
            return@withContext Result.failure(e)
        }
    }
    
    override suspend fun generateQuizQuestions(
        document: Document,
        documentContent: List<DocumentContent>,
        questionCount: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext errorHandler.executeWithRetry {
            val documentText = extractDocumentText(documentContent, maxTokens = MAX_DOCUMENT_TOKENS)
            val prompt = promptGenerator.generateQuizPrompt(document, documentText, questionCount)
            
            val response = generativeModel.generateContent(prompt)
            
            if (response.text != null) {
                response.text!!
            } else {
                throw IOException("Empty response from Gemini")
            }
        }
    }
    
    override suspend fun explainConcept(
        concept: String,
        document: Document,
        documentContent: List<DocumentContent>
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext errorHandler.executeWithRetry {
            val documentText = extractDocumentText(documentContent, maxTokens = MAX_DOCUMENT_TOKENS)
            val prompt = promptGenerator.generateConceptExplanationPrompt(document, documentText, concept)
            
            val response = generativeModel.generateContent(prompt)
            
            if (response.text != null) {
                response.text!!
            } else {
                throw IOException("Empty response from Gemini")
            }
        }
    }
    
    override suspend fun executeCustomPrompt(
        promptTemplate: String,
        document: Document,
        documentContent: List<DocumentContent>
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext errorHandler.executeWithRetry {
            val documentText = extractDocumentText(documentContent, maxTokens = MAX_DOCUMENT_TOKENS)
            val prompt = promptGenerator.generateCustomPrompt(document, documentText, promptTemplate)
            
            val response = generativeModel.generateContent(prompt)
            
            if (response.text != null) {
                response.text!!
            } else {
                throw IOException("Empty response from Gemini")
            }
        }
    }
    
    override suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent("Hello, Gemini! Test connection.")
            if (response.text != null) {
                Result.success("Connection successful: ${response.text}")
            } else {
                Result.failure(IOException("No response from Gemini API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Creates a prompt with document context for answering user queries.
     */
    private fun createDocumentContextPrompt(
        query: String,
        document: Document,
        relevantContent: List<DocumentContent>?
    ): String {
        val documentText = extractDocumentText(relevantContent, maxTokens = MAX_DOCUMENT_TOKENS)
        
        return """
            You are an AI study assistant designed to help students understand their documents and study materials.
            Your task is to provide helpful, informative, and accurate responses based on the document content provided below.
            
            DOCUMENT INFORMATION:
            Title: ${document.title}
            ${document.subject?.let { "Subject: $it\n" } ?: ""}
            ${document.department?.let { "Department: $it\n" } ?: ""}
            
            DOCUMENT CONTENT:
            $documentText
            
            USER QUERY: $query
            
            Please answer the query based only on the information in the document. If you don't find the answer in the document, say so clearly.
            Be helpful, concise, and accurate.
        """.trimIndent()
    }
    
    /**
     * Creates a prompt specifically designed for question answering.
     */
    private fun createQuestionAnswerPrompt(
        question: String,
        document: Document,
        relevantContent: List<DocumentContent>?
    ): String {
        val documentText = extractDocumentText(relevantContent, maxTokens = MAX_DOCUMENT_TOKENS)
        
        return """
            Answer the following question based only on the information in the document.
            
            DOCUMENT: ${document.title}
            
            CONTENT: 
            $documentText
            
            QUESTION: $question
            
            ANSWER:
        """.trimIndent()
    }
    
    /**
     * Converts chat history to the format expected by the Gemini API.
     */
    private fun convertChatHistoryToContent(chatHistory: List<ChatMessage>): List<Content> {
        // Ensure we don't exceed token limits for history
        val history = chatHistory.takeLast(20)  // Limit to last 20 messages for simplicity
        
        return history.map { message ->
            val role = when (message.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "model"
            }
            content(role = role) { text(message.content) }
        }
    }
    
    /**
     * Extracts the text content from document chunks with token limiting.
     */
    private fun extractDocumentText(
        content: List<DocumentContent>?,
        maxTokens: Int = MAX_DOCUMENT_TOKENS
    ): String {
        if (content.isNullOrEmpty()) return "[No document content available]"
        
        // Sort chunks by index to maintain document order
        val sortedChunks = content.sortedBy { it.chunkIndex }
        
        // Estimate total characters based on token limit
        val maxChars = maxTokens * CHARS_PER_TOKEN
        
        val stringBuilder = StringBuilder()
        var currentChars = 0
        
        for (chunk in sortedChunks) {
            // Add section header if available
            chunk.sectionTitle?.let {
                if (it.isNotBlank()) {
                    stringBuilder.append("\n## $it\n\n")
                }
            }
            
            // Add chunk content with character tracking
            val contentToAdd = chunk.content
            if (currentChars + contentToAdd.length <= maxChars) {
                stringBuilder.append(contentToAdd)
                stringBuilder.append("\n\n")
                currentChars += contentToAdd.length + 2
            } else {
                // If adding this chunk would exceed limit, add as much as possible
                val remainingChars = maxChars - currentChars
                if (remainingChars > 100) { // Only add if we can get meaningful content
                    stringBuilder.append(contentToAdd.take(remainingChars))
                    stringBuilder.append("\n[Content truncated due to length]")
                }
                break
            }
        }
        
        return stringBuilder.toString().trim()
    }
    
    /**
     * Splits document content into smaller chunks for processing large documents
     */
    private fun splitIntoChunks(
        documentContent: List<DocumentContent>, 
        maxChunkSize: Int
    ): List<List<DocumentContent>> {
        val result = mutableListOf<List<DocumentContent>>()
        val currentChunk = mutableListOf<DocumentContent>()
        var currentSize = 0
        for (content in documentContent) {
            if (currentSize + content.content.length > maxChunkSize && currentChunk.isNotEmpty()) {
                result.add(currentChunk.toList())
                currentChunk.clear()
                currentSize = 0
            }
            if (content.content.length > maxChunkSize) {
                var splitChunkIndex = 0
                val splitContent = content.content.chunked(maxChunkSize).map { chunk ->
                    DocumentContent(
                        documentId = content.documentId,
                        chunkIndex = splitChunkIndex++,
                        content = chunk,
                        pageNumber = content.pageNumber,
                        extractedDate = content.extractedDate,
                        sectionTitle = content.sectionTitle
                    )
                }
                for (splitPart in splitContent) {
                    result.add(listOf(splitPart))
                }
            } else {
                currentChunk.add(content)
                currentSize += content.content.length
            }
        }
        if (currentChunk.isNotEmpty()) {
            result.add(currentChunk.toList())
        }
        return result
    }
    
    /**
     * Selects the most relevant document chunks for a given query.
     * In a real implementation, this would use semantic search or embedding similarity.
     * This is a simple implementation that selects chunks containing query keywords.
     */
    private fun selectRelevantContent(
        query: String,
        documentContent: List<DocumentContent>?
    ): List<DocumentContent>? {
        if (documentContent.isNullOrEmpty()) return null
        
        // For now, use a simple keyword matching approach
        // Extract keywords from query (words longer than 3 chars)
        val keywords = query.split(Regex("\\W+"))
            .filter { it.length > 3 }
            .map { it.lowercase() }
        
        // If no good keywords, return the first few chunks
        if (keywords.isEmpty()) {
            return documentContent.sortedBy { it.chunkIndex }.take(3)
        }
        // Score chunks based on keyword occurrences
        // Score chunks based on keyword occurrences
        val scoredChunks = documentContent.map<DocumentContent, Pair<DocumentContent, Int>> { chunk ->
            val lowerContent = chunk.content.lowercase()
            val score = keywords.sumOf { keyword: String ->
                if (lowerContent.contains(keyword)) 2.toInt() else 0
            }
            Pair(chunk, score)
        }

        // Select top chunks but maintain document order
        val relevantChunks = scoredChunks
            .filter { pair: Pair<DocumentContent, Int> -> pair.second > 0 }  // Only chunks with at least one keyword match
            .sortedByDescending { pair: Pair<DocumentContent, Int> -> pair.second }  // Sort by relevance score
            .take(5)  // Take top 5 chunks
            .map { pair: Pair<DocumentContent, Int> -> pair.first }  // Extract just the chunks
            .sortedBy { chunk: DocumentContent -> chunk.chunkIndex }  // Restore document order

        return if (relevantChunks.isEmpty()) {
            // Fallback if no matches found
            documentContent.sortedBy { it.chunkIndex }.take(3)
        } else {
            relevantChunks
        }
    }
}
