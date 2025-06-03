package com.noobdev.numlexambuddy.utils

import android.util.Log
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for handling errors when interacting with Gemini API
 */
@Singleton
class GeminiErrorHandler @Inject constructor() {

    
    companion object {
        private const val TAG = "GeminiErrorHandler"
        
        // Error categories
        const val ERROR_QUOTA_EXCEEDED = "QUOTA_EXCEEDED"
        const val ERROR_INVALID_REQUEST = "INVALID_REQUEST"
        const val ERROR_BLOCKED_CONTENT = "BLOCKED_CONTENT"
        const val ERROR_SERVER_ERROR = "SERVER_ERROR"
        const val ERROR_CONNECTION = "CONNECTION_ERROR"
        const val ERROR_UNKNOWN = "UNKNOWN_ERROR"
        
        // Retry configurations
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 1000L
    }
    
    /**
     * Analyze an exception from Gemini API and categorize it
     */
    fun categorizeError(exception: Exception): String {
        val message = exception.message?.lowercase() ?: ""
        return when {
            // Removed GenerativeModelException check, rely on message content only
            (message.contains("quota") || message.contains("rate limit")) -> ERROR_QUOTA_EXCEEDED
            
            message.contains("content blocked") || 
            message.contains("safety") -> ERROR_BLOCKED_CONTENT
            
            message.contains("400") || 
            message.contains("invalid") -> ERROR_INVALID_REQUEST
            
            message.contains("500") || 
            message.contains("503") || 
            message.contains("server") -> ERROR_SERVER_ERROR
            
            message.contains("connect") || 
            message.contains("timeout") || 
            message.contains("network") -> ERROR_CONNECTION
            
            else -> ERROR_UNKNOWN
        }
    }
    
    /**
     * Checks if the error is retryable
     */
    fun isRetryableError(errorCategory: String): Boolean {
        return when (errorCategory) {
            ERROR_SERVER_ERROR, ERROR_CONNECTION -> true
            else -> false
        }
    }
    
    /**
     * Gets a user-friendly error message
     */
    fun getUserFriendlyMessage(errorCategory: String): String {
        return when (errorCategory) {
            ERROR_QUOTA_EXCEEDED -> "API usage limit exceeded. Please try again later."
            ERROR_BLOCKED_CONTENT -> "The content was blocked by AI safety filters."
            ERROR_INVALID_REQUEST -> "Invalid request to the AI service."
            ERROR_SERVER_ERROR -> "AI service is experiencing issues. Please try again later."
            ERROR_CONNECTION -> "Connection issue. Please check your internet and try again."
            else -> "An error occurred while processing your request."
        }
    }
    
    /**
     * Executes a function with retry logic for Gemini API calls
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = MAX_RETRIES,
        block: suspend () -> T
    ): Result<T> {
        var currentDelay = INITIAL_BACKOFF_MS
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                return Result.success(block())
            } catch (e: Exception) {
                lastException = e
                val errorCategory = categorizeError(e)
                
                if (!isRetryableError(errorCategory) || attempt >= maxRetries) {
                    Log.e(TAG, "Error executing Gemini API call (attempt ${attempt+1}/$maxRetries): ${e.message}")
                    return Result.failure(e)
                }
                
                Log.w(TAG, "Retrying Gemini API call after error (attempt ${attempt+1}/$maxRetries): ${e.message}")
            }
            
            // Exponential backoff
            delay(currentDelay)
            currentDelay *= 2
        }
        
        return Result.failure(lastException ?: Exception("Unknown error during API call"))
    }
}
