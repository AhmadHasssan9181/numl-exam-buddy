package com.noobdev.numlexambuddy.data.di

import android.content.Context
import com.noobdev.numlexambuddy.data.dao.ChatDao
import com.noobdev.numlexambuddy.data.dao.DocumentDao
import com.noobdev.numlexambuddy.data.dao.DocumentQueryDao
import com.noobdev.numlexambuddy.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Database module that provides database-related dependencies.
 * This class follows a simple service locator pattern for dependency injection.
 */
object DatabaseModule {
    
    // Application scope for database operations
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Cached database instance
    private var database: AppDatabase? = null
    
    /**
     * Provides the database instance.
     */
    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context, applicationScope)
            database = instance
            instance
        }
    }
    
    /**
     * Provides the document DAO.
     */
    fun provideDocumentDao(context: Context): DocumentDao {
        return provideDatabase(context).documentDao()
    }
    
    /**
     * Provides the document query DAO.
     */
    fun provideDocumentQueryDao(context: Context): DocumentQueryDao {
        return provideDatabase(context).documentQueryDao()
    }
    
    /**
     * Provides the chat DAO.
     */
    fun provideChatDao(context: Context): ChatDao {
        return provideDatabase(context).chatDao()
    }
}
