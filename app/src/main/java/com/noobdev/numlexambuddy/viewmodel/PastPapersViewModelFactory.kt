package com.noobdev.numlexambuddy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.noobdev.numlexambuddy.data.DriveServiceManager
import com.noobdev.numlexambuddy.data.PastPapersRepository

/**
 * Factory for creating PastPapersViewModel with the required dependencies
 * This ensures proper setup of the ViewModel with the service account based Drive API
 */
class PastPapersViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PastPapersViewModel::class.java)) {
            // Create the DriveServiceManager with service account
            val driveServiceManager = DriveServiceManager(context)
            
            // Create the repository with the DriveServiceManager
            val pastPapersRepository = PastPapersRepository(driveServiceManager)
            
            // Return the ViewModel with context and repository
            return PastPapersViewModel(pastPapersRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
