package com.noobdev.numlexambuddy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.noobdev.numlexambuddy.model.ChatMessage
import com.noobdev.numlexambuddy.model.ChatSession
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chat-related entities.
 * Provides methods to interact with chat messages and sessions.
 */
@Dao
interface ChatDao {

    // ChatMessage Operations
    
    /**
     * Insert a new chat message
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long
    
    /**
     * Insert multiple chat messages at once
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>): List<Long>
    
    /**
     * Update an existing chat message
     */
    @Update
    suspend fun updateMessage(message: ChatMessage): Int
    
    /**
     * Delete a chat message
     */
    @Delete
    suspend fun deleteMessage(message: ChatMessage): Int
    
    /**
     * Get a message by its ID
     */
    @Query("SELECT * FROM chat_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): ChatMessage?
    
    /**
     * Get all messages for a document
     */
    @Query("SELECT * FROM chat_messages WHERE document_id = :documentId ORDER BY timestamp ASC")
    suspend fun getMessagesForDocument(documentId: String): List<ChatMessage>
    
    /**
     * Get all messages for a document as Flow
     */
    @Query("SELECT * FROM chat_messages WHERE document_id = :documentId ORDER BY timestamp ASC")
    fun observeMessagesForDocument(documentId: String): Flow<List<ChatMessage>>
    
    /**
     * Get all messages for a document as LiveData
     */
    @Query("SELECT * FROM chat_messages WHERE document_id = :documentId ORDER BY timestamp ASC")
    fun getMessagesForDocumentLiveData(documentId: String): LiveData<List<ChatMessage>>
    
    /**
     * Get messages associated with a specific session
     */
    @Query("""
        SELECT m.* FROM chat_messages m
        INNER JOIN chat_sessions s ON s.document_id = m.document_id
        WHERE s.id = :sessionId
        AND m.timestamp BETWEEN s.created_at AND s.last_updated
        ORDER BY m.timestamp ASC
    """)
    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessage>
    
    /**
     * Delete all messages for a document
     */
    @Query("DELETE FROM chat_messages WHERE document_id = :documentId")
    suspend fun deleteMessagesForDocument(documentId: String): Int
    
    /**
     * Get count of messages for a document
     */
    @Query("SELECT COUNT(*) FROM chat_messages WHERE document_id = :documentId")
    suspend fun getMessageCountForDocument(documentId: String): Int

    // ChatSession Operations
    
    /**
     * Insert a new chat session
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession): Long
    
    /**
     * Update an existing chat session
     */
    @Update
    suspend fun updateSession(session: ChatSession): Int
    
    /**
     * Delete a chat session
     */
    @Delete
    suspend fun deleteSession(session: ChatSession): Int
    
    /**
     * Get a session by its ID
     */
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): ChatSession?
    
    /**
     * Get all sessions for a document
     */
    @Query("SELECT * FROM chat_sessions WHERE document_id = :documentId ORDER BY last_updated DESC")
    suspend fun getSessionsForDocument(documentId: String): List<ChatSession>
    
    /**
     * Get all sessions for a document as Flow
     */
    @Query("SELECT * FROM chat_sessions WHERE document_id = :documentId ORDER BY last_updated DESC")
    fun observeSessionsForDocument(documentId: String): Flow<List<ChatSession>>
    
    /**
     * Get sessions for a document as LiveData
     */
    @Query("SELECT * FROM chat_sessions WHERE document_id = :documentId ORDER BY last_updated DESC")
    fun getSessionsForDocumentLiveData(documentId: String): LiveData<List<ChatSession>>
    
    /**
     * Delete all sessions for a document
     */
    @Query("DELETE FROM chat_sessions WHERE document_id = :documentId")
    suspend fun deleteSessionsForDocument(documentId: String): Int
    
    /**
     * Get most recent chat session for a document
     */
    @Query("SELECT * FROM chat_sessions WHERE document_id = :documentId ORDER BY last_updated DESC LIMIT 1")
    suspend fun getMostRecentSessionForDocument(documentId: String): ChatSession?
    
    /**
     * Update session last updated timestamp
     */
    @Query("UPDATE chat_sessions SET last_updated = :timestamp WHERE id = :sessionId")
    suspend fun updateSessionTimestamp(sessionId: Long, timestamp: Long): Int
    
    /**
     * Update session summary
     */
    @Query("UPDATE chat_sessions SET summary = :summary WHERE id = :sessionId")
    suspend fun updateSessionSummary(sessionId: Long, summary: String): Int
    
    /**
     * Get count of sessions for a document
     */
    @Query("SELECT COUNT(*) FROM chat_sessions WHERE document_id = :documentId")
    suspend fun getSessionCountForDocument(documentId: String): Int
}
