package com.noobdev.numlexambuddy.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation

/**
 * Relationship class to connect ChatSession with its ChatMessages
 */
data class SessionWithMessages(
    @Embedded val session: ChatSession,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "document_id",
        entity = ChatMessage::class
    )
    val messages: List<ChatMessage>
)

/**
 * Relationship class to connect Document with its associated Chat Sessions
 */
data class DocumentWithSessions(
    @Embedded val document: Document,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "document_id",
        entity = ChatSession::class
    )
    val sessions: List<ChatSession>
)

/**
 * Relationship class for retrieving a Document with all its content chunks
 */
data class DocumentWithContent(
    @Embedded val document: Document,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "document_id",
        entity = DocumentContent::class
    )
    val contentChunks: List<DocumentContent>
)

/**
 * Relationship class for retrieving a Document with its metadata
 */
data class DocumentWithMetadata(
    @Embedded val document: Document,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "document_id",
        entity = DocumentMetadata::class
    )
    val metadata: DocumentMetadata?
)

/**
 * Relationship class for retrieving a Document with its summary
 */
data class DocumentWithSummary(
    @Embedded val document: Document,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "documentId",
        entity = DocumentSummary::class
    )
    val summary: DocumentSummary?
)
