package com.noobdev.numlexambuddy.model

import com.noobdev.numlexambuddy.data.DocumentTypeHelper
import java.util.*

/**
 * Extension functions for our document models to add useful operations
 */

/**
 * Returns a formatted display name for a Document
 */
fun Document.getDisplayName(): String {
    return this.title
}

/**
 * Returns a file extension based on document type
 */
fun Document.getFileExtension(): String {
    return when (this.documentType) {
        DocumentType.PDF -> "pdf"
        DocumentType.TEXT -> "txt"
        DocumentType.WORD -> "docx"
        DocumentType.EXCEL -> "xlsx"
        DocumentType.POWERPOINT -> "pptx"
        DocumentType.IMAGE -> "jpg"
        DocumentType.OTHER -> "bin"
    }
}

/**
 * Returns a readable file size string
 */
fun Document.getReadableFileSize(): String {
    return DocumentTypeHelper.getReadableFileSize(this.size)
}

/**
 * Returns true if the document is available for offline access
 */
fun Document.isAvailableOffline(): Boolean {
    return this.status == DocumentStatus.COMPLETE
}

/**
 * Returns true if the document is being downloaded
 */
fun Document.isDownloading(): Boolean {
    return this.status == DocumentStatus.DOWNLOADING || this.status == DocumentStatus.PENDING
}

/**
 * Returns true if the document can be processed with AI
 */
fun Document.canProcessWithAI(): Boolean {
    return DocumentTypeHelper.isSupportedForAI(this.documentType) && 
           this.status == DocumentStatus.COMPLETE
}

/**
 * Gets the time since the document was last accessed in a human-readable format
 */
fun Document.getTimeSinceLastAccessed(): String {
    val now = Date()
    val lastAccessed = this.lastAccessed ?: this.downloadDate
    
    val diffInMillis = now.time - lastAccessed.time
    val diffInSeconds = diffInMillis / 1000
    val diffInMinutes = diffInSeconds / 60
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24
    
    return when {
        diffInDays > 30 -> "${diffInDays / 30} months ago"
        diffInDays > 0 -> "$diffInDays days ago"
        diffInHours > 0 -> "$diffInHours hours ago"
        diffInMinutes > 0 -> "$diffInMinutes minutes ago"
        else -> "Just now"
    }
}
