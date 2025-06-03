package com.noobdev.numlexambuddy.data.di

import android.content.Context
import androidx.room.Room
import com.noobdev.numlexambuddy.data.DocumentContentHelper
import com.noobdev.numlexambuddy.data.DocumentRepository
import com.noobdev.numlexambuddy.data.DocumentSummaryRepository
import com.noobdev.numlexambuddy.data.dao.DocumentSummaryDao
import com.noobdev.numlexambuddy.data.database.AppDatabase
import com.noobdev.numlexambuddy.data.download.DocumentDownloadService
import com.noobdev.numlexambuddy.data.download.DownloadManagerWrapper
import com.noobdev.numlexambuddy.data.storage.DocumentStorageManager
import com.noobdev.numlexambuddy.service.GeminiService
import com.noobdev.numlexambuddy.service.GeminiServiceImpl // You'll need to create this
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DocumentHiltModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "exam_buddy_database"
        ).build()
    }

    @Provides
    fun provideDocumentSummaryDao(database: AppDatabase): DocumentSummaryDao {
        return database.documentSummaryDao()
    }

    @Provides
    @Singleton
    fun provideDocumentStorageManager(@ApplicationContext context: Context): DocumentStorageManager {
        return DocumentStorageManager(context)
    }

    @Provides
    @Singleton
    fun provideDownloadManagerWrapper(@ApplicationContext context: Context): DownloadManagerWrapper {
        return DownloadManagerWrapper(context)
    }

    @Provides
    @Singleton
    fun provideDocumentDownloadService(
        @ApplicationContext context: Context,
        downloadManager: DownloadManagerWrapper,
        storageManager: DocumentStorageManager
    ): DocumentDownloadService {
        return DocumentDownloadService(context, downloadManager, storageManager)
    }

    @Provides
    @Singleton
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

    @Provides
    @Singleton
    fun provideDocumentSummaryRepository(
        documentSummaryDao: DocumentSummaryDao,
        geminiService: GeminiService,
        documentContentHelper: DocumentContentHelper
    ): DocumentSummaryRepository {
        return DocumentSummaryRepository(
            documentSummaryDao = documentSummaryDao,
            geminiService = geminiService,
            documentContentHelper = documentContentHelper
        )
    }
}

