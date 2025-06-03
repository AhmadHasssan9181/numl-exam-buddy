package com.noobdev.numlexambuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.noobdev.numlexambuddy.data.DocumentRepository
import com.noobdev.numlexambuddy.model.ChatMessage
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentContent
import com.noobdev.numlexambuddy.model.MessageRole
import com.noobdev.numlexambuddy.service.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for the document chat feature.
 */
class DocumentChatViewModel(
    private val repository: DocumentRepository,
    private val geminiService: GeminiService
) : ViewModel() {

    // Document being discussed
    private val _document = MutableStateFlow<Document?>(null)
    val document: StateFlow<Document?> = _document.asStateFlow()

    // Document content
    private val _documentContent = MutableStateFlow<DocumentContent?>(null)
    val documentContent: StateFlow<DocumentContent?> = _documentContent.asStateFlow()

    // Chat messages
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load the document for chat.
     */
    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Get the document
                val doc = repository.getDocumentById(documentId)
                _document.value = doc

                // Update last accessed timestamp
                doc?.let {
                    repository.updateLastAccessed(it.id)
                }
            } catch (e: Exception) {
                _error.value = "Error loading document: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load chat messages for the document.
     */
    fun loadMessages(documentId: String) {
        // In a real implementation, this would fetch messages from a database
        // For now, let's just use an empty list and show the document summary as the first message
        _messages.value = emptyList()
    }

    /**
     * Send a new message to Gemini.
     */
    fun sendMessage(content: String) {
        val documentId = _document.value?.id ?: return
        
        // Add user message to the list
        val userMessage = ChatMessage(
            documentId = documentId,
            content = content,
            role = MessageRole.USER,
            timestamp = Date()
        )
        
        _messages.value = _messages.value + userMessage
        
        // Start loading
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Get response from Gemini (this is a placeholder - real implementation would use the Gemini service)
                val response = getResponseFromGemini(content, documentId)
                
                // Add AI response to the list
                val aiMessage = ChatMessage(
                    documentId = documentId,
                    content = response,
                    role = MessageRole.ASSISTANT,
                    timestamp = Date()
                )
                
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                // Add error message
                val errorMessage = ChatMessage(
                    documentId = documentId,
                    content = "Sorry, I encountered an error: ${e.message}",
                    role = MessageRole.ASSISTANT,
                    timestamp = Date(),
                    isError = true
                )
                
                _messages.value = _messages.value + errorMessage
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }    /**
     * Get a response from Gemini using the GeminiService.
     */
    private suspend fun getResponseFromGemini(query: String, documentId: String): String {
        // Get document content if available
        val documentContent = repository.getDocumentContent(documentId)
        
        // Get the document
        val document = _document.value ?: repository.getDocumentById(documentId) 
            ?: throw IllegalStateException("Document not found")
        
        // Get chat history (consider just the last few messages for context)
        val chatHistory = _messages.value.takeLast(10)
        
        // Send query to Gemini service
        val result = geminiService.sendMessage(
            query = query,
            document = document,
            documentContent = documentContent,
            chatHistory = chatHistory
        )
        
        return result.getOrThrow()
    }

    /**
     * Clear the error message.
     */
    fun clearError() {
        _error.value = null
    }
}

/**
 * Factory for creating DocumentChatViewModel instances.
 */
class DocumentChatViewModelFactory(
    private val repository: DocumentRepository,
    private val geminiService: GeminiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DocumentChatViewModel(repository, geminiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
