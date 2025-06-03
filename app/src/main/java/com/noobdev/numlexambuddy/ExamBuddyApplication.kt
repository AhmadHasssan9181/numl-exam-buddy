package com.noobdev.numlexambuddy

import android.app.Application
import com.noobdev.numlexambuddy.data.DocumentContentHelper
import com.noobdev.numlexambuddy.data.DocumentRepository
import com.noobdev.numlexambuddy.data.database.AppDatabase
import com.noobdev.numlexambuddy.data.di.AIServiceModule
import com.noobdev.numlexambuddy.data.di.DatabaseModule
import com.noobdev.numlexambuddy.data.di.DocumentModule
import com.noobdev.numlexambuddy.data.download.DocumentDownloadService
import com.noobdev.numlexambuddy.data.download.DownloadManagerWrapper
import com.noobdev.numlexambuddy.data.parser.DocumentContentManager
import com.noobdev.numlexambuddy.data.parser.DocumentParserFactory
import com.noobdev.numlexambuddy.data.storage.DocumentStorageManager
import com.noobdev.numlexambuddy.service.GeminiService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Custom application class for the app.
 * Initializes important components like the database.
 */
@HiltAndroidApp
class ExamBuddyApplication : Application() {
    
    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Lazily initialize database
    private val database by lazy {
        DatabaseModule.provideDatabase(this)
    }
    
    // Document system components
    val parserFactory by lazy { DocumentModule.provideDocumentParserFactory() }
    
    val contentManager by lazy { DocumentModule.provideDocumentContentManager(parserFactory) }
    
    val documentStorageManager by lazy { DocumentModule.provideDocumentStorageManager(this) }
    
    val downloadManagerWrapper by lazy { DocumentModule.provideDownloadManagerWrapper(this) }
    
    val documentDownloadService by lazy { 
        DocumentModule.provideDocumentDownloadService(
            this, 
            downloadManagerWrapper, 
            documentStorageManager
        ) 
    }

    // Add documentContentHelper instance
    val documentContentHelper by lazy { DocumentContentHelper() }

    val documentRepository by lazy {
        DocumentModule.provideDocumentRepository(
            database,
            documentStorageManager,
            documentDownloadService,
            documentContentHelper
        )
    }
    
    // AI Service components
    val geminiService by lazy {
        AIServiceModule.provideGeminiService(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any other components or services here
    }
}
