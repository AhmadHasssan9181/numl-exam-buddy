package com.noobdev.numlexambuddy.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

/**
 * DocumentSummary entity to store AI-generated summaries of documents
 * Stored separately from Document to handle large text fields efficiently
 */
@Entity(
    tableName = "document_summaries",
    foreignKeys = [
        ForeignKey(
            entity = Document::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
@TypeConverters(DateConverter::class, StringListConverter::class)
data class DocumentSummary(
    @PrimaryKey
    val documentId: String,      // Associated document ID (foreign key)
    
    @ColumnInfo(name = "summary_text")
    val summaryText: String,     // Full AI-generated summary
    
    @ColumnInfo(name = "key_points")
    val keyPoints: List<String> = emptyList(), // Key points extracted from document
    
    @ColumnInfo(name = "generated_date")
    val generatedDate: Date,     // When the summary was generated
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Date        // When the summary was last updated
)
