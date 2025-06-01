package com.noobdev.numlexambuddy.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for handling schema changes between versions.
 */
object DatabaseMigrations {

    /**
     * Example migration from version 1 to 2.
     * Uncomment and implement when needed for the first schema change.
     */
    /*
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Implement schema changes here
            // Example:
            // database.execSQL("ALTER TABLE documents ADD COLUMN new_column TEXT")
        }
    }
    */

    /**
     * Returns all the migrations.
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            // Add migrations here as they are created
            // MIGRATION_1_2,
            // MIGRATION_2_3,
            // etc.
        )
    }
}
