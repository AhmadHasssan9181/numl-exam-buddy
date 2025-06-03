package com.noobdev.numlexambuddy.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.noobdev.numlexambuddy.data.DocumentRepository
import com.noobdev.numlexambuddy.data.download.DocumentDownloadStatus
import com.noobdev.numlexambuddy.data.parser.DocumentContentManager
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentContent
import com.noobdev.numlexambuddy.model.DocumentStatus
import com.noobdev.numlexambuddy.model.DocumentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for managing documents.
 */
class DocumentViewModel(
    private val repository: DocumentRepository,
    private val contentManager: DocumentContentManager
) : ViewModel() {

    // State for all documents
    val documents = repository.getAllDocuments()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // State for completed documents
    val completedDocuments = repository.getDocumentsByStatus(DocumentStatus.COMPLETE)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // State for recent documents
    val recentDocuments = repository.getRecentDocuments(5)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // State for downloading documents
    val downloadingDocuments = repository.getDocumentsByStatus(DocumentStatus.DOWNLOADING)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Selected document
    private val _selectedDocument = MutableStateFlow<Document?>(null)
    val selectedDocument: StateFlow<Document?> = _selectedDocument.asStateFlow()
    
    // Selected document content
    private val _documentContent = MutableStateFlow<DocumentContent?>(null)
    val documentContent: StateFlow<DocumentContent?> = _documentContent.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Select a document.
     */
    fun selectDocument(document: Document) {
        _selectedDocument.value = document
        
        // Update last accessed timestamp
        viewModelScope.launch {
            repository.updateLastAccessed(document.id)
            
            // Load document content
            loadDocumentContent(document)
        }
    }

    /**
     * Load the content of a document.
     */
    fun loadDocumentContent(document: Document) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val contentList = contentManager.extractDocumentContent(document)
                _documentContent.value = contentList.firstOrNull()
            } catch (e: Exception) {
                _error.value = "Error loading document content: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Import a document from a URI.
     */
    fun importDocument(
        uri: Uri,
        fileName: String,
        mimeType: String,
        sourceUrl: String,
        subject: String? = null,
        semester: Int? = null,
        department: String? = null,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val document = repository.importDocumentFromUri(
                    uri = uri,
                    fileName = fileName,
                    mimeType = mimeType,
                    sourceUrl = sourceUrl,
                    subject = subject,
                    semester = semester,
                    department = department,
                    tags = tags
                )
                
                if (document != null) {
                    _selectedDocument.value = document
                } else {
                    _error.value = "Failed to import document"
                }
            } catch (e: Exception) {
                _error.value = "Error importing document: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Download a document from a URL.
     */
    fun downloadDocument(
        sourceUrl: String,
        fileName: String,
        mimeType: String,
        estimatedSize: Long = 0,
        subject: String? = null,
        semester: Int? = null,
        department: String? = null,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val document = repository.downloadDocument(
                    sourceUrl = sourceUrl,
                    fileName = fileName,
                    mimeType = mimeType,
                    estimatedSize = estimatedSize,
                    subject = subject,
                    semester = semester,
                    department = department,
                    tags = tags
                )
                
                if (document != null) {
                    _selectedDocument.value = document
                } else {
                    _error.value = "Failed to start document download"
                }
            } catch (e: Exception) {
                _error.value = "Error downloading document: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cancel a document download.
     */
    fun cancelDownload(documentId: String) {
        repository.cancelDownload(documentId)
    }

    /**
     * Delete a document.
     */
    fun deleteDocument(document: Document, deleteFile: Boolean = true) {
        viewModelScope.launch {
            try {
                repository.deleteDocument(document, deleteFile)
                
                // If this was the selected document, clear it
                if (_selectedDocument.value?.id == document.id) {
                    _selectedDocument.value = null
                    _documentContent.value = null
                }
            } catch (e: Exception) {
                _error.value = "Error deleting document: ${e.message}"
            }
        }
    }

    /**
     * Update document metadata.
     */
    fun updateDocumentMetadata(
        document: Document,
        newTitle: String? = null,
        newSubject: String? = null,
        newSemester: Int? = null,
        newDepartment: String? = null,
        newTags: List<String>? = null
    ) {
        viewModelScope.launch {
            try {
                repository.updateDocumentMetadata(
                    document = document,
                    newTitle = newTitle,
                    newSubject = newSubject,
                    newSemester = newSemester,
                    newDepartment = newDepartment,
                    newTags = newTags
                )
                
                // If this was the selected document, refresh it
                if (_selectedDocument.value?.id == document.id) {
                    _selectedDocument.value = repository.getDocumentById(document.id)
                }
            } catch (e: Exception) {
                _error.value = "Error updating document metadata: ${e.message}"
            }
        }
    }

    /**
     * Update a document's summary.
     */
    fun updateDocumentSummary(documentId: String, summary: String) {
        viewModelScope.launch {
            try {
                repository.updateDocumentSummary(documentId, summary)
                
                // If this was the selected document, refresh it
                if (_selectedDocument.value?.id == documentId) {
                    _selectedDocument.value = repository.getDocumentById(documentId)
                }
            } catch (e: Exception) {
                _error.value = "Error updating document summary: ${e.message}"
            }
        }
    }

    /**
     * Search documents by title.
     */
    fun searchDocuments(query: String): StateFlow<List<Document>> {
        return repository.searchDocumentsByTitle(query)
            .catch { emit(emptyList()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * Get documents by type.
     */
    fun getDocumentsByType(documentType: DocumentType): StateFlow<List<Document>> {
        return repository.getDocumentsByType(documentType)
            .catch { emit(emptyList()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * Clear the error.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clean up resources when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}

/**
 * Factory for creating DocumentViewModel instances.
 */
class DocumentViewModelFactory(
    private val repository: DocumentRepository,
    private val contentManager: DocumentContentManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DocumentViewModel(repository, contentManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
