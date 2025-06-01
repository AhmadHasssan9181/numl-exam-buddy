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
 * Models for Document Download and Storage Feature
 */

/**
 * Document Status represents the current state of a document
 */
enum class DocumentStatus {
    PENDING,     // Document is queued for download
    DOWNLOADING, // Document is currently downloading
    COMPLETE,    // Document is downloaded and ready
    FAILED,      // Document download failed
    DELETED      // Document was deleted but DB record remains
}

/**
 * Document Type represents the type of document
 */
enum class DocumentType {
    PDF,
    TEXT,
    WORD,
    EXCEL,
    POWERPOINT,
    IMAGE,
    OTHER
}

/**
 * Document entity for Room database
 */
@Entity(tableName = "documents")
@TypeConverters(DateConverter::class, DocumentTypeConverter::class, DocumentStatusConverter::class, StringListConverter::class)
data class Document(
    @PrimaryKey
    override val id: String,      // Unique identifier for the document

    @ColumnInfo(name = "title")
    override val title: String,   // Title of the document

    @ColumnInfo(name = "file_path")
    val filePath: String,         // Local storage path

    @ColumnInfo(name = "mime_type")
    val mimeType: String,         // MIME type of the document

    @ColumnInfo(name = "size")
    val size: Long,               // Size in bytes

    @ColumnInfo(name = "source_url")
    val sourceUrl: String,        // Original URL where document was downloaded from

    @ColumnInfo(name = "document_type")
    val documentType: DocumentType, // Type of document (PDF, TEXT, etc.)

    @ColumnInfo(name = "status")
    val status: DocumentStatus,   // Status of document (DOWNLOADING, COMPLETE, etc.)

    @ColumnInfo(name = "download_date")
    val downloadDate: Date,       // Date when document was downloaded

    @ColumnInfo(name = "last_accessed")
    val lastAccessed: Date?,      // Date when document was last accessed

    @ColumnInfo(name = "subject")
    override val subject: String?,         // Associated subject

    @ColumnInfo(name = "semester")
    override val semester: Int?,           // Associated semester

    @ColumnInfo(name = "department")
    override val department: String?,      // Associated department
    @ColumnInfo(name = "summary")
    val summary: String? = null,  // AI-generated summary of the document

    @ColumnInfo(name = "tags")
    override val tags: List<String> = emptyList(), // Tags for categorization and search

    @ColumnInfo(name = "created_at")
    override val createdAt: Date = downloadDate, // Creation date (using download date)

    @ColumnInfo(name = "updated_at")
    override val updatedAt: Date? = lastAccessed // Last update date
) : DocumentBase, Timestamped, Categorizable {
    override fun getDisplayName(): String = title
}

/**
 * Chat message model for conversations with Gemini about documents
 */
@Entity(
    tableName = "chat_messages",
    indices = [
        Index("document_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = Document::class,
            parentColumns = ["id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(DateConverter::class, MessageRoleConverter::class)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,            // Message ID (auto-generated)
    
    @ColumnInfo(name = "document_id")
    val documentId: String,      // Associated document ID
    
    @ColumnInfo(name = "content")
    val content: String,         // Message content
    
    @ColumnInfo(name = "role")
    val role: MessageRole,       // Role (USER or ASSISTANT)
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Date,         // Message timestamp
    
    @ColumnInfo(name = "is_error")
    val isError: Boolean = false // Whether this is an error message
)

/**
 * Role of a message in a conversation
 */
enum class MessageRole {
    USER,       // Message from user
    ASSISTANT   // Response from Gemini
}

/**
 * Session to group chat messages related to a specific document interaction
 */
@Entity(
    tableName = "chat_sessions",
    indices = [
        Index("document_id")
    ],
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
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,           // Session ID (auto-generated)
    
    @ColumnInfo(name = "document_id")
    val documentId: String,     // Associated document ID
    
    @ColumnInfo(name = "title")
    val title: String,          // Session title
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,        // Creation timestamp
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Date,      // Last update timestamp
    
    @ColumnInfo(name = "summary")
    val summary: String? = null // Session summary or topic
)

/**
 * Type converters for Room database
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class DocumentTypeConverter {
    @TypeConverter
    fun fromDocumentType(value: DocumentType): String {
        return value.name
    }

    @TypeConverter
    fun toDocumentType(value: String): DocumentType {
        return try {
            DocumentType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            DocumentType.OTHER
        }
    }
}

class DocumentStatusConverter {
    @TypeConverter
    fun fromDocumentStatus(value: DocumentStatus): String {
        return value.name
    }

    @TypeConverter
    fun toDocumentStatus(value: String): DocumentStatus {
        return try {
            DocumentStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            DocumentStatus.FAILED
        }
    }
}

class MessageRoleConverter {
    @TypeConverter
    fun fromMessageRole(value: MessageRole): String {
        return value.name
    }

    @TypeConverter
    fun toMessageRole(value: String): MessageRole {
        return try {
            MessageRole.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MessageRole.USER
        }
    }
}

class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",")
        }
    }
}
