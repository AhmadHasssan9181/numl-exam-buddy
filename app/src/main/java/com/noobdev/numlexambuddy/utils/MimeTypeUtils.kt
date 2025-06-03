package com.noobdev.numlexambuddy.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.util.Locale

/**
 * Utility class for MIME type and file extension operations.
 */
object MimeTypeUtils {

    // Common MIME types and their extensions
    private val mimeTypeToExtensionMap = mapOf(
        // PDF
        "application/pdf" to "pdf",
        
        // Text
        "text/plain" to "txt",
        "text/html" to "html",
        "text/csv" to "csv",
        "text/markdown" to "md",
        
        // Microsoft Office
        "application/msword" to "doc",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "docx",
        "application/vnd.ms-excel" to "xls",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" to "xlsx",
        "application/vnd.ms-powerpoint" to "ppt",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" to "pptx",
        
        // Open Document Format
        "application/vnd.oasis.opendocument.text" to "odt",
        "application/vnd.oasis.opendocument.spreadsheet" to "ods",
        "application/vnd.oasis.opendocument.presentation" to "odp",
        
        // Image
        "image/jpeg" to "jpg",
        "image/png" to "png",
        "image/gif" to "gif",
        "image/webp" to "webp",
        "image/tiff" to "tiff",
        "image/bmp" to "bmp",
        
        // Others
        "application/zip" to "zip",
        "application/x-rar-compressed" to "rar",
        "application/epub+zip" to "epub"
    )
    
    /**
     * Gets the extension for a MIME type.
     * @param mimeType The MIME type.
     * @return The extension, or null if the MIME type is unknown.
     */
    fun getExtensionFromMimeType(mimeType: String): String {
        // Try the mapping first
        mimeTypeToExtensionMap[mimeType]?.let {
            return it
        }
        
        // Fall back to system
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
    }
    
    /**
     * Gets the MIME type from a file extension.
     * @param extension The file extension.
     * @return The MIME type, or a generic binary type if the extension is unknown.
     */
    fun getMimeTypeFromExtension(extension: String): String {
        val extensionLower = extension.lowercase(Locale.getDefault()).trim('.')
        
        // Try to find the MIME type in the map
        mimeTypeToExtensionMap.entries.find { it.value == extensionLower }?.let {
            return it.key
        }
        
        // Fall back to system
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extensionLower) 
            ?: "application/octet-stream"
    }
    
    /**
     * Gets the MIME type from a URI.
     * @param context The application context.
     * @param uri The URI.
     * @return The MIME type, or a generic binary type if it couldn't be determined.
     */
    fun getMimeTypeFromUri(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        
        // Try content resolver first
        contentResolver.getType(uri)?.let {
            return it
        }
        
        // Try to get the extension from the filename
        var filename: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex)
                }
            }
        }
        
        if (filename != null) {
            val dotIndex = filename!!.lastIndexOf('.')
            if (dotIndex > 0 && dotIndex < filename!!.length - 1) {
                val extension = filename!!.substring(dotIndex + 1).lowercase(Locale.getDefault())
                
                // Try to get MIME type from extension
                val mimeType = getMimeTypeFromExtension(extension)
                if (mimeType != "application/octet-stream") {
                    return mimeType
                }
            }
        }
        
        // If all else fails, return generic binary type
        return "application/octet-stream"
    }
    
    /**
     * Attempts to detect the MIME type from file content (magic numbers).
     * @param filePath The path to the file.
     * @return The detected MIME type, or null if it couldn't be determined.
     */
    fun detectMimeTypeFromContent(filePath: String): String? {
        try {
            BufferedInputStream(FileInputStream(filePath)).use { bis ->
                bis.mark(16)
                val magic = ByteArray(16)
                val read = bis.read(magic)
                bis.reset()
                
                if (read < 4) return null
                
                // Check file signatures (magic numbers)
                return when {
                    // PDF: %PDF
                    magic[0] == 0x25.toByte() && magic[1] == 0x50.toByte() &&
                            magic[2] == 0x44.toByte() && magic[3] == 0x46.toByte() -> 
                        "application/pdf"
                    
                    // ZIP-based formats (docx, xlsx, pptx, odt, etc.)
                    magic[0] == 0x50.toByte() && magic[1] == 0x4B.toByte() &&
                            magic[2] == 0x03.toByte() && magic[3] == 0x04.toByte() -> {
                        // We'd need to check the internal structure to determine exact type
                        // But this is a ZIP file at minimum
                        "application/zip"
                    }
                    
                    // JPEG: FFD8
                    magic[0] == 0xFF.toByte() && magic[1] == 0xD8.toByte() -> 
                        "image/jpeg"
                    
                    // PNG: 89 50 4E 47
                    magic[0] == 0x89.toByte() && magic[1] == 0x50.toByte() &&
                            magic[2] == 0x4E.toByte() && magic[3] == 0x47.toByte() -> 
                        "image/png"
                    
                    // GIF: GIF87a or GIF89a
                    magic[0] == 0x47.toByte() && magic[1] == 0x49.toByte() &&
                            magic[2] == 0x46.toByte() && magic[3] == 0x38.toByte() &&
                            (magic[4] == 0x37.toByte() || magic[4] == 0x39.toByte()) &&
                            magic[5] == 0x61.toByte() -> 
                        "image/gif"
                    
                    // DOC (Microsoft Office)
                    magic[0] == 0xD0.toByte() && magic[1] == 0xCF.toByte() &&
                            magic[2] == 0x11.toByte() && magic[3] == 0xE0.toByte() -> 
                        "application/msword"
                    
                    else -> null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Gets a display-friendly name for a MIME type.
     * @param mimeType The MIME type.
     * @return A readable name for the MIME type.
     */
    fun getReadableName(mimeType: String): String {
        return when {
            mimeType.contains("pdf") -> "PDF Document"
            mimeType.contains("text/plain") -> "Text Document"
            mimeType.contains("text/html") -> "HTML Document"
            mimeType.contains("text/csv") -> "CSV Spreadsheet"
            mimeType.contains("msword") || mimeType.contains("wordprocessing") -> "Word Document"
            mimeType.contains("excel") || mimeType.contains("spreadsheet") -> "Excel Spreadsheet"
            mimeType.contains("powerpoint") || mimeType.contains("presentation") -> "PowerPoint Presentation"
            mimeType.contains("image/") -> "Image"
            mimeType.contains("audio/") -> "Audio"
            mimeType.contains("video/") -> "Video"
            mimeType.contains("zip") -> "ZIP Archive"
            mimeType.contains("rar") -> "RAR Archive"
            mimeType.contains("epub") -> "E-Book"
            else -> "File"
        }
    }
    
    /**
     * Gets the file name from a URI.
     * @param context The application context.
     * @param uri The URI.
     * @return The file name, or null if it couldn't be determined.
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var filename: String? = null
        
        // Try to get the display name from the content resolver
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex)
                }
            }
        }
        
        // If we couldn't get the name from the content resolver, try the last path segment
        if (filename == null) {
            filename = uri.lastPathSegment
        }
        
        return filename
    }
}
