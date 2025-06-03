package com.noobdev.numlexambuddy.data.parser

import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.StringBuilder

/**
 * Parser for PDF documents.
 * Uses iText library to extract text from PDF files.
 */
class PDFParser : DocumentParser {
    companion object {
        private const val TAG = "PDFParser"
        private val SUPPORTED_MIME_TYPES = listOf(
            "application/pdf"
        )
        private const val MAX_PAGES_TO_PARSE = 100 // Limit for large PDFs
    }

    override suspend fun parseDocument(filePath: String): String? = withContext(Dispatchers.IO) {
        var pdfDocument: PdfDocument? = null
        
        try {
            val file = File(filePath)
            
            // Check if file exists
            if (!file.exists()) {
                Log.e(TAG, "PDF file does not exist: $filePath")
                return@withContext null
            }
            
            // Open the PDF document
            val reader = PdfReader(file)
            pdfDocument = PdfDocument(reader)
            
            // Get the number of pages
            val numberOfPages = pdfDocument.numberOfPages
            val pagesToProcess = minOf(numberOfPages, MAX_PAGES_TO_PARSE)
            
            // Extract text from each page
            val textBuilder = StringBuilder()
            
            for (i in 1..pagesToProcess) {
                try {
                    val page = pdfDocument.getPage(i)
                    val text = PdfTextExtractor.getTextFromPage(page)
                    
                    textBuilder.append(text)
                    textBuilder.append("\n\n")
                } catch (e: Exception) {
                    Log.w(TAG, "Error extracting text from page $i: ${e.message}")
                    textBuilder.append("[Error extracting text from page $i]\n\n")
                }
            }
            
            // Add a note if the document was truncated
            if (numberOfPages > MAX_PAGES_TO_PARSE) {
                textBuilder.append("\n... Content truncated. Only the first $MAX_PAGES_TO_PARSE pages were processed ...\n")
                textBuilder.append("Total pages in document: $numberOfPages")
            }
            
            return@withContext textBuilder.toString()
        } catch (e: IOException) {
            Log.e(TAG, "Error parsing PDF: ${e.message}", e)
            return@withContext null
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory while parsing PDF: $filePath", e)
            return@withContext "PDF too large to parse in memory. Consider extracting specific pages."
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error parsing PDF: ${e.message}", e)
            return@withContext null
        } finally {
            try {
                pdfDocument?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing PDF document", e)
            }
        }
    }

    override fun canParseType(mimeType: String): Boolean {
        return SUPPORTED_MIME_TYPES.any { 
            mimeType.equals(it, ignoreCase = true) || mimeType.startsWith("$it;")
        }
    }

    override fun getParserName(): String {
        return "PDF Parser"
    }
    
    /**
     * Extract text from specific pages of a PDF.
     * Useful for very large PDFs where extracting all text would be memory-intensive.
     * 
     * @param filePath The path to the PDF file
     * @param pageRange The range of pages to extract (e.g. 1..5)
     * @return The extracted text from the specified pages
     */
    suspend fun parseSpecificPages(filePath: String, pageRange: IntRange): String? = withContext(Dispatchers.IO) {
        var pdfDocument: PdfDocument? = null
        
        try {
            val file = File(filePath)
            
            // Check if file exists
            if (!file.exists()) {
                return@withContext null
            }
            
            // Open the PDF document
            val reader = PdfReader(file)
            pdfDocument = PdfDocument(reader)
            
            // Get the number of pages
            val numberOfPages = pdfDocument.numberOfPages
            
            // Adjust page range if needed
            val adjustedRange = pageRange.start.coerceAtLeast(1)..pageRange.endInclusive.coerceAtMost(numberOfPages)
            
            // Extract text from each page in the range
            val textBuilder = StringBuilder()
            
            for (i in adjustedRange) {
                try {
                    val page = pdfDocument.getPage(i)
                    val text = PdfTextExtractor.getTextFromPage(page)
                    
                    textBuilder.append("--- Page $i ---\n\n")
                    textBuilder.append(text)
                    textBuilder.append("\n\n")
                } catch (e: Exception) {
                    Log.w(TAG, "Error extracting text from page $i: ${e.message}")
                    textBuilder.append("[Error extracting text from page $i]\n\n")
                }
            }
            
            return@withContext textBuilder.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing specific PDF pages: ${e.message}", e)
            return@withContext null
        } finally {
            try {
                pdfDocument?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing PDF document", e)
            }
        }
    }
}
