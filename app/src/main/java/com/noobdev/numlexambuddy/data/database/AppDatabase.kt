package com.noobdev.numlexambuddy.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.noobdev.numlexambuddy.data.dao.ChatDao
import com.noobdev.numlexambuddy.data.dao.DocumentDao
import com.noobdev.numlexambuddy.data.dao.DocumentQueryDao
import com.noobdev.numlexambuddy.model.ChatMessage
import com.noobdev.numlexambuddy.model.ChatSession
import com.noobdev.numlexambuddy.model.DateConverter
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentStatusConverter
import com.noobdev.numlexambuddy.model.DocumentTypeConverter
import com.noobdev.numlexambuddy.model.MessageRoleConverter
import com.noobdev.numlexambuddy.model.StringListConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The Room database for the application.
 */
@Database(
    entities = [
        Document::class,
        ChatMessage::class,
        ChatSession::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    DocumentTypeConverter::class,
    DocumentStatusConverter::class,
    MessageRoleConverter::class,
    StringListConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Returns the DAO for accessing Document entities.
     */
    abstract fun documentDao(): DocumentDao

    /**
     * Returns the DAO for complex document queries.
     */
    abstract fun documentQueryDao(): DocumentQueryDao

    /**
     * Returns the DAO for accessing Chat entities.
     */
    abstract fun chatDao(): ChatDao

    companion object {
        // Singleton instance
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Database name
        private const val DATABASE_NAME = "numl_exam_buddy_db"

        /**
         * Gets the singleton database instance.
         */
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            // If the instance exists, return it; otherwise create a new database instance
            return INSTANCE ?: synchronized(this) {                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // Add migration strategies
                    .addMigrations(*DatabaseMigrations.getAllMigrations())
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration() // Fallback if migration fails
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Database callback for database initialization or migrations.
         */
        private class AppDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            /**
             * Called when database is created for the first time.
             * Use this to populate initial data if needed.
             */
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                // You can perform initial database operations here
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Add any initial data here if needed
                    }
                }
            }

            /**
             * Called when database is opened.
             */
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Any operations needed when database is opened can go here
            }
        }
    }
}
