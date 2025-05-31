package com.noobdev.numlexambuddy.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.noobdev.numlexambuddy.model.Lecture
import com.noobdev.numlexambuddy.data.DriveServiceManager
import com.noobdev.numlexambuddy.data.LecturesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LecturesViewModel(
    private val repository: LecturesRepository,
    private val context: Context
) : ViewModel() {

    // UI state variables
    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects: StateFlow<List<String>> = _subjects

    private val _lectures = MutableStateFlow<List<Lecture>>(emptyList())
    val lectures: StateFlow<List<Lecture>> = _lectures

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
                _lectures.value = emptyList()

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
                val semesterName = "SEMESTER-$semester"
                _subjects.value = repository.getSubjects(degree, semesterName)

                // Clear previous lectures selection
                _lectures.value = emptyList()

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
                val semesterName = "SEMESTER-$semester"
                _lectures.value = repository.getLectures(degree, semesterName, subject)

            } catch (e: Exception) {
                _error.value = "Error loading lectures: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Helper function to open a lecture
    fun openLecture(lecture: Lecture) {
        viewModelScope.launch {
            try {
                if (lecture.downloadUrl.isNotEmpty()) {
                    // Open the Google Drive file link in browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lecture.downloadUrl))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    _error.value = "View link not available for this lecture"
                }
            } catch (e: Exception) {
                _error.value = "Error opening lecture: ${e.message}"
            }
        }
    }

    // Add this method after openLecture()
    fun clearError() {
        _error.value = null
    }
}

/**
 * Factory for creating LecturesViewModel with the required dependencies
 */
class LecturesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LecturesViewModel::class.java)) {
            // Create the DriveServiceManager with service account
            val driveServiceManager = DriveServiceManager(context)
            
            // Create the repository with the DriveServiceManager
            val lecturesRepository = LecturesRepository(driveServiceManager)
            
            // Return the ViewModel with context and repository
            return LecturesViewModel(lecturesRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
