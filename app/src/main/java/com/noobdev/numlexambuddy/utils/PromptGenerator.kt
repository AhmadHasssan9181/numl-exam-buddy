package com.noobdev.numlexambuddy.utils

import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentContent
import com.noobdev.numlexambuddy.model.DocumentType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for generating prompts for Gemini API based on document content
 */
@Singleton
class PromptGenerator @Inject constructor() {
    
    companion object {
        // Token limits
        private const val MAX_CONTEXT_TOKENS = 30000
        
        // Prompt templates
        private  val SUMMARIZE_TEMPLATE = """
            Please provide a concise summary of the following %s titled "%s".
            Focus on the main points, key concepts, and important conclusions.
            
            CONTENT:
            %s
            
            SUMMARY:
        """.trimIndent()
        
        private  val QUESTION_ANSWERING_TEMPLATE = """
            Given the following %s titled "%s", please answer this question:
            
            QUESTION: %s
            
            CONTENT:
            %s
            
            ANSWER:
        """.trimIndent()
        
        private  val QUIZ_GENERATION_TEMPLATE = """
            Based on the following %s titled "%s", please generate %d multiple-choice quiz questions 
            to test understanding of the key concepts. For each question, provide 4 options and indicate the correct answer.
            
            CONTENT:
            %s
            
            QUIZ QUESTIONS:
        """.trimIndent()
        
        private  val EXPLAIN_CONCEPT_TEMPLATE = """
            In the following %s titled "%s", find information about the concept "%s" and explain it thoroughly.
            If the concept is not directly mentioned, use the content to infer relevant information or indicate that it's not covered.
            
            CONTENT:
            %s
            
            EXPLANATION:
        """.trimIndent()
    }
    
    /**
     * Generates a prompt for summarizing a document
     */
    fun generateSummaryPrompt(document: Document, documentContent: String): String {
        val documentType = getDocumentTypeDescription(document.documentType)
        return SUMMARIZE_TEMPLATE.format(documentType, document.title, documentContent)
    }
    
    /**
     * Generates a prompt for answering a question about a document
     */
    fun generateQuestionPrompt(
        document: Document, 
        documentContent: String, 
        question: String
    ): String {
        val documentType = getDocumentTypeDescription(document.documentType)
        return QUESTION_ANSWERING_TEMPLATE.format(documentType, document.title, question, documentContent)
    }
    
    /**
     * Generates a prompt for creating a quiz based on document content
     */
    fun generateQuizPrompt(
        document: Document, 
        documentContent: String, 
        questionCount: Int = 5
    ): String {
        val documentType = getDocumentTypeDescription(document.documentType)
        return QUIZ_GENERATION_TEMPLATE.format(documentType, document.title, questionCount, documentContent)
    }
    
    /**
     * Generates a prompt for explaining a concept in the document
     */
    fun generateConceptExplanationPrompt(
        document: Document, 
        documentContent: String, 
        concept: String
    ): String {
        val documentType = getDocumentTypeDescription(document.documentType)
        return EXPLAIN_CONCEPT_TEMPLATE.format(documentType, document.title, concept, documentContent)
    }
    
    /**
     * Generates a custom prompt with the document content
     */
    fun generateCustomPrompt(
        document: Document, 
        documentContent: String, 
        promptTemplate: String
    ): String {
        return promptTemplate
            .replace("%TITLE%", document.title)
            .replace("%CONTENT%", documentContent)
            .replace("%TYPE%", getDocumentTypeDescription(document.documentType))    }
    
    /**
     * Gets a user-friendly description of the document type
     */
    private fun getDocumentTypeDescription(type: DocumentType): String {
        return when (type) {
            DocumentType.PDF -> "PDF document"
            DocumentType.TEXT -> "text document"
            DocumentType.WORD -> "Word document"
            DocumentType.EXCEL -> "Excel spreadsheet"
            DocumentType.POWERPOINT -> "PowerPoint presentation"
            DocumentType.IMAGE -> "image"
            DocumentType.OTHER -> "document"
        }
    }
    
    /**
     * Prepares document content for a prompt, ensuring it fits within token limits
     */
    fun prepareDocumentContentForPrompt(
        contents: List<DocumentContent>, 
        maxTokens: Int = MAX_CONTEXT_TOKENS
    ): String {
        val totalText = contents.joinToString("\n\n") { it.content }
        
        // If content is small enough, return as is
        if (totalText.length / 4 <= maxTokens) { // Rough estimate of tokens
            return totalText
        }
        
        // Otherwise, truncate while preserving structure
        val estimatedCharsPerToken = 4
        val maxChars = maxTokens * estimatedCharsPerToken
        
        return when {
            contents.size == 1 -> {
                // Single content piece, just truncate
                contents[0].content.take(maxChars) + "\n[Content truncated due to length...]"
            }
            contents.size > 1 -> {
                // Multiple pieces, distribute token budget proportionally
                val charsPerPiece = maxChars / contents.size
                val truncatedPieces = contents.map {
                    if (it.content.length <= charsPerPiece) {
                        it.content
                    } else {
                        it.content.take(charsPerPiece) + "\n[Portion truncated due to length...]"
                    }
                }
                truncatedPieces.joinToString("\n\n")
            }
            else -> ""
        }
    }
}
