package com.noobdev.numlexambuddy.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noobdev.numlexambuddy.model.StudyMaterial
import com.noobdev.numlexambuddy.data.StudyMaterialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StudyMaterialUiState(
    val departments: List<String> = emptyList(),
    val semesters: List<String> = emptyList(),
    val subjects: List<String> = emptyList(),
    val studyMaterials: List<StudyMaterial> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StudyMaterialViewModel(
    private val repository: StudyMaterialRepository,
    private val context: Context
) : ViewModel() {    private val _departments = MutableStateFlow<List<String>>(emptyList())
    private val _semesters = MutableStateFlow<List<String>>(emptyList())
    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    private val _studyMaterials = MutableStateFlow<List<StudyMaterial>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
      val uiState: StateFlow<StudyMaterialUiState> = combine(
        combine(_departments, _semesters, _subjects) { departments, semesters, subjects ->
            Triple(departments, semesters, subjects)
        },
        combine(_studyMaterials, _isLoading, _error) { studyMaterials, isLoading, error ->
            Triple(studyMaterials, isLoading, error)
        }
    ) { (departments, semesters, subjects), (studyMaterials, isLoading, error) ->
        StudyMaterialUiState(
            departments = departments,
            semesters = semesters,
            subjects = subjects,
            studyMaterials = studyMaterials,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyMaterialUiState()
    )

    fun loadDepartments() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val departments = listOf(
                    "BSCS", "BSSE", "BSIT", "BSAI", "BSDS",
                    "MCS", "MIT", "MSCS", "PhD-CS"
                )
                _departments.value = departments
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error loading departments: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSemesters(department: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val semesters = (1..8).map { "SEMESTER-$it" }
                _semesters.value = semesters
                _subjects.value = emptyList()
                _studyMaterials.value = emptyList()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error loading semesters: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSubjects(department: String, semester: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _subjects.value = repository.getSubjects(department, semester)
                _studyMaterials.value = emptyList()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error loading subjects: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadStudyMaterials(department: String, semester: String, subject: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _studyMaterials.value = repository.getStudyMaterials(department, semester, subject)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error loading study materials: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadStudyMaterial(studyMaterial: StudyMaterial) {
        viewModelScope.launch {
            try {
                if (studyMaterial.downloadUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(studyMaterial.downloadUrl))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    _error.value = "Download link not available for this material"
                }
            } catch (e: Exception) {
                _error.value = "Error opening download link: ${e.message}"
            }
        }
    }    fun clearError() {
        _error.value = null
    }
}
