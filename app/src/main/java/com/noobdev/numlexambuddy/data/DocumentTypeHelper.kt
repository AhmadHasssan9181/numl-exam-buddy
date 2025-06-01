package com.noobdev.numlexambuddy.data

import com.noobdev.numlexambuddy.model.DocumentType
import java.io.File

/**
 * Utility class for document file type detection and metadata
 */
object DocumentTypeHelper {
    /**
     * Determines the document type based on MIME type or file extension
     */
    fun detectDocumentType(mimeType: String?, fileName: String?): DocumentType {
        // First try to determine from MIME type
        if (!mimeType.isNullOrEmpty()) {
            when {
                mimeType.contains("pdf", ignoreCase = true) -> return DocumentType.PDF
                mimeType.contains("text", ignoreCase = true) -> return DocumentType.TEXT
                mimeType.contains("word") || mimeType.contains("docx") || mimeType.contains("doc") -> return DocumentType.WORD
                mimeType.contains("excel") || mimeType.contains("spreadsheet") || mimeType.contains("xlsx") || mimeType.contains("xls") -> return DocumentType.EXCEL
                mimeType.contains("powerpoint") || mimeType.contains("presentation") || mimeType.contains("pptx") || mimeType.contains("ppt") -> return DocumentType.POWERPOINT
                mimeType.contains("image") -> return DocumentType.IMAGE
            }
        }

        // If MIME type checking failed, try with file extension
        if (!fileName.isNullOrEmpty()) {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return when (extension) {
                "pdf" -> DocumentType.PDF
                "txt", "md", "rtf" -> DocumentType.TEXT
                "doc", "docx", "odt" -> DocumentType.WORD
                "xls", "xlsx", "csv", "ods" -> DocumentType.EXCEL
                "ppt", "pptx", "odp" -> DocumentType.POWERPOINT
                "jpg", "jpeg", "png", "gif", "bmp", "webp" -> DocumentType.IMAGE
                else -> DocumentType.OTHER
            }
        }

        // Default fallback
        return DocumentType.OTHER
    }

    /**
     * Gets file extension from a file path or URL
     */
    fun getFileExtension(path: String): String {
        return path.substringAfterLast('.', "")
    }

    /**
     * Checks if file is supported for AI processing
     */
    fun isSupportedForAI(documentType: DocumentType): Boolean {
        return when (documentType) {
            DocumentType.PDF, DocumentType.TEXT, DocumentType.WORD -> true
            else -> false
        }
    }

    /**
     * Gets expected MIME type based on document type
     */
    fun getMimeType(documentType: DocumentType): String {
        return when (documentType) {
            DocumentType.PDF -> "application/pdf"
            DocumentType.TEXT -> "text/plain"
            DocumentType.WORD -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            DocumentType.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            DocumentType.POWERPOINT -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            DocumentType.IMAGE -> "image/jpeg"
            DocumentType.OTHER -> "application/octet-stream"
        }
    }

    /**
     * Gets readable document type name for UI display
     */
    fun getReadableTypeName(documentType: DocumentType): String {
        return when (documentType) {
            DocumentType.PDF -> "PDF Document"
            DocumentType.TEXT -> "Text Document"
            DocumentType.WORD -> "Word Document"
            DocumentType.EXCEL -> "Excel Spreadsheet"
            DocumentType.POWERPOINT -> "PowerPoint Presentation"
            DocumentType.IMAGE -> "Image"
            DocumentType.OTHER -> "Document"
        }
    }

    /**
     * Get file size as a readable string
     */
    fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    /**
     * Get file size from a file path
     */
    fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists()) file.length() else 0
    }
}
