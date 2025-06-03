package com.noobdev.numlexambuddy.service

import com.noobdev.numlexambuddy.model.ChatMessage
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentContent

/**
 * Service interface for interacting with Google Gemini API.
 * Provides methods for document-based chat conversations.
 */
interface GeminiService {
    /**
     * Sends a message to Gemini and gets a response.
     *
     * @param query The user message
     * @param document The document context
     * @param documentContent Optional document content chunks
     * @param chatHistory Previous messages in the conversation
     * @return The response from Gemini
     */
    suspend fun sendMessage(
        query: String,
        document: Document,
        documentContent: List<DocumentContent>?,
        chatHistory: List<ChatMessage> = emptyList()
    ): Result<String>

    /**
     * Generates a summary for a document.
     *
     * @param document The document to summarize
     * @param documentContent Content chunks of the document
     * @return The generated summary
     */
    suspend fun generateSummary(
        document: Document,
        documentContent: List<DocumentContent>
    ): Result<String>

    /**
     * Answers a specific question about a document.
     *
     * @param question The user's question
     * @param document The document context
     * @param documentContent Content chunks of the document
     * @return The answer to the question
     */
    suspend fun answerQuestion(
        question: String,
        document: Document,
        documentContent: List<DocumentContent>
    ): Result<String>

    /**
     * Extracts key information from a document.
     *
     * @param document The document to analyze
     * @param documentContent Content chunks of the document
     * @param extractionType The type of information to extract (e.g., "key points", "terms")
     * @return The extracted information
     */
    suspend fun extractInformation(
        document: Document,
        documentContent: List<DocumentContent>,
        extractionType: String
    ): Result<String>

    /**
     * Generates quiz questions based on document content
     *
     * @param document The document to use
     * @param documentContent Content chunks of the document
     * @param questionCount Number of questions to generate
     * @return Generated quiz questions
     */
    suspend fun generateQuizQuestions(
        document: Document,
        documentContent: List<DocumentContent>,
        questionCount: Int = 5
    ): Result<String>

    /**
     * Explains a specific concept from the document
     *
     * @param concept The concept to explain
     * @param document The document context
     * @param documentContent Content chunks of the document
     * @return Explanation of the concept
     */
    suspend fun explainConcept(
        concept: String,
        document: Document,
        documentContent: List<DocumentContent>
    ): Result<String>

    /**
     * Executes a custom prompt with the document content
     *
     * @param promptTemplate Custom prompt template
     * @param document The document context
     * @param documentContent Content chunks of the document
     * @return Generated result
     */
    suspend fun executeCustomPrompt(
        promptTemplate: String,
        document: Document,
        documentContent: List<DocumentContent>
    ): Result<String>

    /**
     * Tests the API connection with a simple request
     * @return Result with success message if connection works, error otherwise
     */
    suspend fun testConnection(): Result<String>
}
