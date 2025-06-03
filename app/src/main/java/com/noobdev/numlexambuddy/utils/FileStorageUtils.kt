package com.noobdev.numlexambuddy.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Utility class for file storage operations.
 */
object FileStorageUtils {

    // Constants for file directories
    private const val DOCUMENTS_DIR = "documents"
    private const val TEMP_DIR = "temp"
    private const val CACHE_DIR = "cache"
    private const val BUFFER_SIZE = 8192

    /**
     * Generates a unique file path for a document in the app's files directory.
     * @param context The application context.
     * @param fileName The name of the file.
     * @param documentType The type of the document.
     * @return The file path for the document.
     */
    fun generateDocumentFilePath(context: Context, fileName: String, documentType: DocumentType): String {
        val documentsDir = getDocumentsDirectory(context)
        val sanitizedFileName = sanitizeFileName(fileName)
        val fileExtension = getFileExtension(sanitizedFileName, documentType)
        val uniqueFileName = "${UUID.randomUUID()}-$sanitizedFileName.$fileExtension"
        
        return File(documentsDir, uniqueFileName).absolutePath
    }

    /**
     * Gets the file extension for a document type.
     * @param fileName Original file name, used to extract extension if available.
     * @param documentType The type of the document.
     * @return The file extension.
     */
    private fun getFileExtension(fileName: String, documentType: DocumentType): String {
        // First try to extract extension from file name
        val dotIndex = fileName.lastIndexOf('.')
        if (dotIndex > 0 && dotIndex < fileName.length - 1) {
            return fileName.substring(dotIndex + 1)
        }

        // If no extension in file name, use default for document type
        return when (documentType) {
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
     * Sanitizes a file name to remove any invalid characters.
     * @param fileName The file name to sanitize.
     * @return The sanitized file name.
     */
    private fun sanitizeFileName(fileName: String): String {
        return fileName
            .replace("[\\\\/:*?\"<>|]".toRegex(), "_") // Replace invalid file name characters
            .take(100) // Limit length to avoid path too long errors
    }

    /**
     * Gets or creates the documents directory.
     * @param context The application context.
     * @return The documents directory.
     */
    private fun getDocumentsDirectory(context: Context): File {
        val directory = File(context.filesDir, DOCUMENTS_DIR)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    /**
     * Gets or creates a temporary directory for file operations.
     * @param context The application context.
     * @return The temporary directory.
     */
    fun getTempDirectory(context: Context): File {
        val directory = File(context.cacheDir, TEMP_DIR)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    /**
     * Checks if there's enough storage space available.
     * @param context The application context.
     * @param requiredBytes The number of bytes required.
     * @return true if there's enough space, false otherwise.
     */
    fun isStorageAvailable(context: Context, requiredBytes: Long): Boolean {
        val filesDir = context.filesDir
        val availableSpace = filesDir.usableSpace
        return availableSpace >= requiredBytes
    }

    /**
     * Gets the size of a file.
     * @param filePath The path of the file.
     * @return The size of the file in bytes, or 0 if the file doesn't exist.
     */
    fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists() && file.isFile) file.length() else 0L
    }

    /**
     * Gets the file size from a content URI.
     * @param context The application context.
     * @param contentUri The content URI.
     * @return The size of the file in bytes, or 0 if the size couldn't be determined.
     */
    fun getFileSizeFromUri(context: Context, contentUri: Uri): Long {
        val contentResolver = context.contentResolver
        
        // Try to get size from DocumentFile
        val documentFile = DocumentFile.fromSingleUri(context, contentUri)
        if (documentFile != null && documentFile.exists()) {
            val length = documentFile.length()
            if (length > 0) return length
        }
        
        // Try to get size from content resolver
        try {
            contentResolver.query(contentUri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    if (sizeIndex != -1) {
                        return cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // If all else fails, try to read the entire input stream
        try {
            contentResolver.openInputStream(contentUri)?.use { inputStream ->
                return inputStream.available().toLong()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return 0L
    }

    /**
     * Detects the MIME type of a file.
     * @param context The application context.
     * @param uri The URI of the file.
     * @return The MIME type of the file, or null if it couldn't be determined.
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        return contentResolver.getType(uri) ?: getMimeTypeFromExtension(uri.toString())
    }

    /**
     * Gets the MIME type from a file extension.
     * @param url The URL or file path.
     * @return The MIME type, or null if it couldn't be determined.
     */
    private fun getMimeTypeFromExtension(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        } else null
    }

    /**
     * Detects the document type from a MIME type.
     * @param mimeType The MIME type.
     * @return The document type.
     */
    fun detectDocumentType(mimeType: String): DocumentType {
        return when {
            mimeType.contains("pdf") -> DocumentType.PDF
            mimeType.contains("text") || mimeType.contains("txt") -> DocumentType.TEXT
            mimeType.contains("word") || mimeType.contains("doc") -> DocumentType.WORD
            mimeType.contains("excel") || mimeType.contains("sheet") || mimeType.contains("xls") -> DocumentType.EXCEL
            mimeType.contains("powerpoint") || mimeType.contains("presentation") || mimeType.contains("ppt") -> DocumentType.POWERPOINT
            mimeType.contains("image") -> DocumentType.IMAGE
            else -> DocumentType.OTHER
        }
    }

    /**
     * Saves an input stream to a file.
     * @param inputStream The input stream to save.
     * @param destinationPath The destination file path.
     * @return true if the operation was successful, false otherwise.
     */
    fun saveInputStreamToFile(inputStream: InputStream, destinationPath: String): Boolean {
        try {
            // Create directories if they don't exist
            val destinationFile = File(destinationPath)
            destinationFile.parentFile?.mkdirs()
            
            // Copy the input stream to the output file
            FileOutputStream(destinationFile).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                
                output.flush()
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Saves a file from a URI to the app's internal storage.
     * @param context The application context.
     * @param sourceUri The source URI.
     * @param fileName The name for the saved file.
     * @param documentType The type of the document.
     * @return The path to the saved file, or null if the operation failed.
     */
    fun saveFileFromUri(
        context: Context,
        sourceUri: Uri,
        fileName: String,
        documentType: DocumentType
    ): String? {
        val destinationPath = generateDocumentFilePath(context, fileName, documentType)
        
        try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                if (saveInputStreamToFile(inputStream, destinationPath)) {
                    return destinationPath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return null
    }

    /**
     * Gets a URI for sharing a file.
     * @param context The application context.
     * @param filePath The path of the file.
     * @return A content URI for the file.
     */
    fun getShareableUri(context: Context, filePath: String): Uri {
        val file = File(filePath)
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
    }

    /**
     * Deletes a file.
     * @param filePath The path of the file to delete.
     * @return true if the file was deleted, false otherwise.
     */
    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.delete()
    }
    
    /**
     * Creates a temporary file for storing downloaded content.
     * @param context The application context.
     * @param extension The file extension.
     * @return The created temporary file.
     */
    fun createTempFile(context: Context, extension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val tempDir = getTempDirectory(context)
        return File.createTempFile("TEMP_${timeStamp}_", ".$extension", tempDir)
    }

    /**
     * Cleans the temporary directory by removing files older than a certain age.
     * @param context The application context.
     * @param maxAgeMs The maximum age of files in milliseconds.
     */
    fun cleanTempDirectory(context: Context, maxAgeMs: Long = 24 * 60 * 60 * 1000) {
        val tempDir = getTempDirectory(context)
        val currentTime = System.currentTimeMillis()
        
        tempDir.listFiles()?.forEach { file ->
            if (currentTime - file.lastModified() > maxAgeMs) {
                file.delete()
            }
        }
    }

    /**
     * Formats a file size for display.
     * @param sizeBytes The size in bytes.
     * @return A formatted string representation of the file size.
     */
    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        
        return String.format("%.1f %s", sizeBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    /**
     * Share a document with other apps.
     * @param context The application context.
     * @param file The file to share.
     * @param mimeType The MIME type of the file.
     */
    fun shareDocument(context: Context, file: File, mimeType: String) {
        try {
            val fileUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
            
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_STREAM, fileUri)
                type = mimeType
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Document"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
    }

    /**
     * Open a document with an appropriate app.
     * @param context The application context.
     * @param file The file to open.
     * @param mimeType The MIME type of the file.
     */
    fun openDocument(context: Context, file: File, mimeType: String) {
        try {
            val fileUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
            
            val viewIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_VIEW
                setDataAndType(fileUri, mimeType)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error - no app available to handle this file type
        }
    }
}
