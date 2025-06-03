package com.noobdev.numlexambuddy.data.parser

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Parser for plain text files.
 * This parser can handle text files with various extensions.
 */
class PlainTextParser : DocumentParser {
    companion object {
        private const val TAG = "PlainTextParser"
        private val SUPPORTED_MIME_TYPES = listOf(
            "text/plain",
            "text/html",
            "text/csv",
            "text/markdown",
            "text/xml",
            "application/json"
        )
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10 MB
    }

    override suspend fun parseDocument(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            
            // Check if file exists
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: $filePath")
                return@withContext null
            }
            
            // Check file size
            if (file.length() > MAX_FILE_SIZE) {
                Log.w(TAG, "File is too large for complete parsing: $filePath (${file.length()} bytes)")
                // For large files, read only the first part to avoid memory issues
                return@withContext readLargeTextFile(file)
            }
            
            // Read the file content
            return@withContext file.readText()
        } catch (e: IOException) {
            Log.e(TAG, "Error parsing text file: ${e.message}", e)
            return@withContext null
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory while parsing text file: $filePath", e)
            return@withContext "File too large to parse in memory. Consider breaking it into smaller parts."
        }
    }

    override fun canParseType(mimeType: String): Boolean {
        return SUPPORTED_MIME_TYPES.any { 
            mimeType.equals(it, ignoreCase = true) || mimeType.startsWith("$it;")
        }
    }

    override fun getParserName(): String {
        return "Plain Text Parser"
    }
    
    /**
     * Read a large text file by chunking it.
     * This method reads only the first chunk and returns a message indicating that the file was truncated.
     * 
     * @param file The file to read
     * @return The first chunk of the file with a message indicating truncation
     */
    private fun readLargeTextFile(file: File): String {
        val reader = file.bufferedReader()
        val buffer = StringBuilder()
        val chunkSize = 1 * 1024 * 1024 // 1 MB chunk
        var bytesRead = 0
        
        reader.use {
            val charBuffer = CharArray(8192) // 8KB read buffer
            var read: Int
            
            while (it.read(charBuffer).also { read = it } != -1) {
                buffer.append(charBuffer, 0, read)
                bytesRead += read * 2 // Rough estimate of bytes (2 bytes per char)
                
                if (bytesRead >= chunkSize) {
                    break
                }
            }
        }
        
        // Add a note that the file was truncated
        if (file.length() > bytesRead) {
            buffer.append("\n\n... Content truncated. File too large to display completely ...")
        }
        
        return buffer.toString()
    }
}
