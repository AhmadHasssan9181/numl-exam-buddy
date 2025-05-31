package com.noobdev.numlexambuddy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.noobdev.numlexambuddy.data.*
import com.noobdev.numlexambuddy.utils.GoogleDriveHelper

/**
 * Factory for creating ViewModels with the required dependencies
 * This ensures proper setup of ViewModels with the Google Drive API
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        
        return when {
            modelClass.isAssignableFrom(LecturesViewModel::class.java) -> {
                // Use DriveServiceManager for LecturesRepository
                val driveServiceManager = DriveServiceManager(context)
                val repository = LecturesRepository(driveServiceManager)
                LecturesViewModel(repository, context) as T
            }
            
            modelClass.isAssignableFrom(StudyMaterialViewModel::class.java) -> {
                // Create the Google Drive service for StudyMaterialRepository
                val driveService = GoogleDriveHelper.createDriveService(context)
                val repository = StudyMaterialRepository(driveService)
                StudyMaterialViewModel(repository, context) as T
            }
            
            modelClass.isAssignableFrom(ProjectsViewModel::class.java) -> {
                // Create the Google Drive service for ProjectsRepository
                val driveService = GoogleDriveHelper.createDriveService(context)
                val repository = ProjectsRepository(driveService)
                ProjectsViewModel(repository, context) as T
            }
            
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
