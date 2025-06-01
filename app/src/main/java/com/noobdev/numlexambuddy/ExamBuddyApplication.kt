package com.noobdev.numlexambuddy

import android.app.Application
import com.noobdev.numlexambuddy.data.database.AppDatabase
import com.noobdev.numlexambuddy.data.di.DatabaseModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Custom application class for the app.
 * Initializes important components like the database.
 */
class ExamBuddyApplication : Application() {
    
    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Lazily initialize database
    private val database by lazy {
        DatabaseModule.provideDatabase(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any other components or services here
    }
}
