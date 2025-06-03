package com.noobdev.numlexambuddy.data.di

import android.content.Context
import com.noobdev.numlexambuddy.data.DocumentContentHelper
import com.noobdev.numlexambuddy.data.DocumentRepository
import com.noobdev.numlexambuddy.data.download.DocumentDownloadService
import com.noobdev.numlexambuddy.data.download.DownloadManagerWrapper
import com.noobdev.numlexambuddy.data.parser.DocumentContentManager
import com.noobdev.numlexambuddy.data.parser.DocumentParserFactory
import com.noobdev.numlexambuddy.data.storage.DocumentStorageManager
import com.noobdev.numlexambuddy.data.database.AppDatabase

/**
 * Dependency Injection module for document-related components.
 * This helps manage the dependencies between the different components of the document system.
 */
object DocumentModule {

    /**
     * Provide a DocumentParserFactory.
     */
    fun provideDocumentParserFactory(): DocumentParserFactory {
        return DocumentParserFactory()
    }

    /**
     * Provide a DocumentContentManager.
     */
    fun provideDocumentContentManager(parserFactory: DocumentParserFactory): DocumentContentManager {
        return DocumentContentManager(parserFactory)
    }

    /**
     * Provide a DocumentStorageManager.
     */
    fun provideDocumentStorageManager(context: Context): DocumentStorageManager {
        return DocumentStorageManager(context)
    }

    /**
     * Provide a DownloadManagerWrapper.
     */
    fun provideDownloadManagerWrapper(context: Context): DownloadManagerWrapper {
        return DownloadManagerWrapper(context)
    }

    /**
     * Provide a DocumentDownloadService.
     */
    fun provideDocumentDownloadService(
        context: Context,
        downloadManager: DownloadManagerWrapper,
        storageManager: DocumentStorageManager
    ): DocumentDownloadService {
        return DocumentDownloadService(context, downloadManager, storageManager)
    }

    /**
     * Provide a DocumentRepository.
     */
    fun provideDocumentRepository(
        database: AppDatabase,
        storageManager: DocumentStorageManager,
        downloadService: DocumentDownloadService,
        documentContentHelper: DocumentContentHelper
    ): DocumentRepository {
        return DocumentRepository(
            documentDao = database.documentDao(),
            documentQueryDao = database.documentQueryDao(),
            storageManager = storageManager,
            downloadService = downloadService,
            documentContentHelper = documentContentHelper
        )
    }
}
