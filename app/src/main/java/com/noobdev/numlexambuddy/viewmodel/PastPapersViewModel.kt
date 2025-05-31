package com.noobdev.numlexambuddy.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.noobdev.numlexambuddy.Screens.Department
import com.noobdev.numlexambuddy.Screens.Paper
import com.noobdev.numlexambuddy.data.DriveServiceManager
import com.noobdev.numlexambuddy.data.PastPapersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PastPapersViewModel(
    private val repository: PastPapersRepository,
    private val context: Context
) : ViewModel() {

    // UI state variables
    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects: StateFlow<List<String>> = _subjects

    private val _papers = MutableStateFlow<List<Paper>>(emptyList())
    val papers: StateFlow<List<Paper>> = _papers

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Called when a department is selected
    fun selectDegree(degree: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                // Get all semesters, but we won't update UI yet
                // (our UI already has fixed semesters 1-8)
                repository.getSemesters(degree)

                // Clear previous selections
                _subjects.value = emptyList()
                _papers.value = emptyList()

            } catch (e: Exception) {
                _error.value = "Error loading semesters: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Called when a semester is selected
    fun selectSemester(degree: String, semester: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val semesterName = "Semester-$semester"
                _subjects.value = repository.getSubjects(degree, semesterName)

                // Clear previous papers selection
                _papers.value = emptyList()

            } catch (e: Exception) {
                _error.value = "Error loading subjects: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Called when a subject is selected
    fun selectSubject(degree: String, semester: Int, subject: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val semesterName = "Semester-$semester"
                _papers.value = repository.getPastPapers(degree, semesterName, subject)

            } catch (e: Exception) {
                _error.value = "Error loading papers: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }    // Helper function to download a paper
    fun downloadPaper(paper: Paper) {
        // In a real app, you'd implement downloading functionality here
        // For now, we'll just show a message
        _error.value = "Download functionality coming soon!"
    }
    // Add this method after downloadPaper()
    fun clearError() {
        _error.value = null
    }
}