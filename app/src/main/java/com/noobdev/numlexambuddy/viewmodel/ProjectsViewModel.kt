package com.noobdev.numlexambuddy.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noobdev.numlexambuddy.model.Project
import com.noobdev.numlexambuddy.data.ProjectsRepository
import com.noobdev.numlexambuddy.utils.GoogleDriveHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProjectsUiState(
    val isLoading: Boolean = false,
    val projects: List<Project> = emptyList(),
    val departments: List<String> = emptyList(),
    val semesters: List<String> = emptyList(),
    val subjects: List<String> = emptyList(),
    val error: String? = null
)

class ProjectsViewModel(
    private val repository: ProjectsRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    fun loadDepartments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val departments = repository.getDepartments()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    departments = departments
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load departments"
                )
            }
        }
    }

    fun loadSemesters(department: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val semesters = repository.getSemesters(department)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    semesters = semesters,
                    subjects = emptyList(),
                    projects = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load semesters"
                )
            }
        }
    }

    fun loadSubjects(department: String, semester: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val subjects = repository.getSubjects(department, semester)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    subjects = subjects,
                    projects = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load subjects"
                )
            }
        }
    }

    fun loadProjects(department: String, semester: String, subject: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val projects = repository.getProjects(department, semester, subject)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    projects = projects
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load projects"
                )
            }
        }
    }

    fun downloadProject(project: Project) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(project.downloadUrl)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to open project: ${e.message}"
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
