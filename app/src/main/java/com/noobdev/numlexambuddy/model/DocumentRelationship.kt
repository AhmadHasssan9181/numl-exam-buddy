package com.noobdev.numlexambuddy.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import androidx.room.TypeConverter
import java.util.Date

/**
 * DocumentRelationship entity to represent relationships between documents
 * This allows for organizing documents in collections, sequences, or parent-child structures
 */
@Entity(
    tableName = "document_relationships",
    indices = [
        Index("source_document_id"),
        Index("target_document_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = Document::class,
            parentColumns = ["id"],
            childColumns = ["source_document_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Document::class,
            parentColumns = ["id"],
            childColumns = ["target_document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(DateConverter::class, RelationshipTypeConverter::class)
data class DocumentRelationship(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "source_document_id")
    val sourceDocumentId: String,  // Source document ID (parent)
    
    @ColumnInfo(name = "target_document_id")
    val targetDocumentId: String,  // Target document ID (child)
    
    @ColumnInfo(name = "relationship_type")
    val relationshipType: RelationshipType, // Type of relationship
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,           // When the relationship was created
    
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,        // For ordered relationships (sequence)
    
    @ColumnInfo(name = "notes")
    val notes: String? = null      // Optional notes about relationship
)

/**
 * Type of relationship between documents
 */
enum class RelationshipType {
    RELATED,       // General relationship - documents are related
    SEQUENCE,      // Sequential relationship - documents are ordered
    PARENT_CHILD,  // Hierarchical relationship - document is part of another
    TRANSLATION,   // Document is a translation of another
    VERSION        // Document is a version/revision of another
}

/**
 * Type converter for RelationshipType enum
 */
class RelationshipTypeConverter {
    @TypeConverter
    fun fromRelationshipType(value: RelationshipType): String {
        return value.name
    }
    
    @TypeConverter
    fun toRelationshipType(value: String): RelationshipType {
        return try {
            RelationshipType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RelationshipType.RELATED
        }
    }
}
