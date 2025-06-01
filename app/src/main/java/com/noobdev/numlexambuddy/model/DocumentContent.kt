package com.noobdev.numlexambuddy.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

/**
 * DocumentContent entity to store extracted content from documents
 * Content is stored in chunks to facilitate efficient RAG with Gemini
 */
@Entity(
    tableName = "document_content",
    indices = [Index("document_id")],
    foreignKeys = [
        ForeignKey(
            entity = Document::class,
            parentColumns = ["id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(DateConverter::class)
data class DocumentContent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "document_id")
    val documentId: String,      // Associated document ID
    
    @ColumnInfo(name = "chunk_index")
    val chunkIndex: Int,         // Index of this chunk within document
    
    @ColumnInfo(name = "content")
    val content: String,         // Text content of this chunk
    
    @ColumnInfo(name = "page_number")
    val pageNumber: Int? = null, // Page number (for PDFs)
    
    @ColumnInfo(name = "extracted_date")
    val extractedDate: Date,     // When the content was extracted
    
    @ColumnInfo(name = "section_title")
    val sectionTitle: String? = null // Title of section (if detected)
)

/**
 * DocumentMetadata contains metadata extracted from the document content
 * This helps with search and AI features
 */
@Entity(
    tableName = "document_metadata",
    indices = [Index("document_id")],
    foreignKeys = [
        ForeignKey(
            entity = Document::class,
            parentColumns = ["id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(DateConverter::class, StringListConverter::class)
data class DocumentMetadata(
    @PrimaryKey
    val documentId: String,      // Associated document ID
    
    @ColumnInfo(name = "author")
    val author: String? = null,  // Author of the document
    
    @ColumnInfo(name = "created_date")
    val createdDate: Date? = null, // When document was originally created
    
    @ColumnInfo(name = "modified_date")
    val modifiedDate: Date? = null, // When document was last modified
    
    @ColumnInfo(name = "keywords")
    val keywords: List<String> = emptyList(), // Extracted keywords
    
    @ColumnInfo(name = "topics")
    val topics: List<String> = emptyList(),   // Extracted main topics
    
    @ColumnInfo(name = "language")
    val language: String? = null, // Detected language
    
    @ColumnInfo(name = "page_count")
    val pageCount: Int? = null,  // Number of pages (for PDFs)
    
    @ColumnInfo(name = "word_count")
    val wordCount: Int? = null,  // Approximate word count
    
    @ColumnInfo(name = "extracted_date")
    val extractedDate: Date      // When metadata was extracted
)
