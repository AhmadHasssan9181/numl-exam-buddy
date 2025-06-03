package com.noobdev.numlexambuddy.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for handling schema changes between versions.
 */
object DatabaseMigrations {    /**
     * Migration from version 1 to 2.
     * Adds DocumentSummary table for storing document summaries.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create document_summaries table
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `document_summaries` (
                    `documentId` TEXT NOT NULL, 
                    `summary` TEXT NOT NULL, 
                    `generatedAt` INTEGER NOT NULL, 
                    `isComplete` INTEGER NOT NULL DEFAULT 1, 
                    PRIMARY KEY(`documentId`),
                    FOREIGN KEY(`documentId`) REFERENCES `documents`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """
            )
            
            // Create index on documentId
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_document_summaries_documentId` ON `document_summaries` (`documentId`)"
            )
        }
    }    /**
     * Returns all the migrations.
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            // Add migrations here as they are created
            MIGRATION_1_2,
            // MIGRATION_2_3,
            // etc.
        )
    }
}
