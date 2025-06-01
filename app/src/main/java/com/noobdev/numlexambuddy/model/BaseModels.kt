package com.noobdev.numlexambuddy.model

import java.util.Date

/**
 * Base interface for document-related classes to standardize common operations
 */
interface DocumentBase {
    val id: String
    val title: String
    
    /**
     * Returns a user-friendly display label for the document
     */
    fun getDisplayName(): String
}

/**
 * Interface for entities with timestamps
 */
interface Timestamped {
    val createdAt: Date
    val updatedAt: Date?
}

/**
 * Interface for entities that can be categorized
 */
interface Categorizable {
    val subject: String?
    val semester: Int?
    val department: String?
    val tags: List<String>
}
