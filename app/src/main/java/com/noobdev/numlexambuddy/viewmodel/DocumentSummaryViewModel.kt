package com.noobdev.numlexambuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noobdev.numlexambuddy.data.DocumentRepository
import com.noobdev.numlexambuddy.data.DocumentSummaryRepository
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling document summaries
 */
@HiltViewModel
class DocumentSummaryViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val summaryRepository: DocumentSummaryRepository
) : ViewModel() {

    // UI state for summary loading
    private val _summaryState = MutableStateFlow<UiState<String>>(UiState.Initial)
    val summaryState: StateFlow<UiState<String>> = _summaryState.asStateFlow()

    /**
     * Loads a summary for a document, generating if needed
     */
    fun loadSummary(documentId: String) {
        viewModelScope.launch {
            _summaryState.value = UiState.Loading
            
            try {
                val document = documentRepository.getDocumentById(documentId)
                if (document == null) {
                    _summaryState.value = UiState.Error("Document not found")
                    return@launch
                }
                
                val result = summaryRepository.getDocumentSummary(document)
                
                if (result.isSuccess) {
                    _summaryState.value = UiState.Success(result.getOrThrow())
                } else {
                    _summaryState.value = UiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to generate summary"
                    )
                }
            } catch (e: Exception) {
                _summaryState.value = UiState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Refreshes a document summary
     */
    fun refreshSummary(documentId: String) {
        viewModelScope.launch {
            _summaryState.value = UiState.Loading
            
            try {
                // Invalidate cached summary
                summaryRepository.invalidateSummary(documentId)
                
                // Reload summary
                loadSummary(documentId)
            } catch (e: Exception) {
                _summaryState.value = UiState.Error("Error refreshing summary: ${e.message}")
            }
        }
    }
}
