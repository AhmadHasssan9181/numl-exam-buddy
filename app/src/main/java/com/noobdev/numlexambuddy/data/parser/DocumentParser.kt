package com.noobdev.numlexambuddy.data.parser

/**
 * Interface for document parsers.
 * Implementations of this interface can parse different types of documents.
 */
interface DocumentParser {
    /**
     * Parse a document and extract its content.
     * 
     * @param filePath The path to the document file
     * @return The extracted content as a string, or null if parsing failed
     */
    suspend fun parseDocument(filePath: String): String?
    
    /**
     * Check if this parser can parse a document with the given mime type.
     * 
     * @param mimeType The mime type of the document
     * @return True if this parser can handle the document, false otherwise
     */
    fun canParseType(mimeType: String): Boolean
    
    /**
     * Get the name of the parser.
     * 
     * @return The name of the parser
     */
    fun getParserName(): String
}

/**
 * Factory for creating document parsers.
 * This factory can create a parser for a given mime type.
 */
class DocumentParserFactory {
    private val parsers = mutableListOf<DocumentParser>()
    
    init {
        // Register parsers
        parsers.add(PlainTextParser())
        parsers.add(PDFParser())
    }
    
    /**
     * Get a parser for a document with the given mime type.
     * 
     * @param mimeType The mime type of the document
     * @return A parser that can parse the document, or null if no parser is available
     */
    fun getParserForType(mimeType: String): DocumentParser? {
        return parsers.find { it.canParseType(mimeType) }
    }
    
    /**
     * Register a new parser.
     * 
     * @param parser The parser to register
     */
    fun registerParser(parser: DocumentParser) {
        parsers.add(parser)
    }
}
