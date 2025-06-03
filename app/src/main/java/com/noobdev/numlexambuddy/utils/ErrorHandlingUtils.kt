package com.noobdev.numlexambuddy.utils

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for handling errors, particularly with downloads
 */
@Singleton
class ErrorHandlingUtils @Inject constructor(
    private val context: Context
) {
    /**
     * Checks if the device has an internet connection
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getActiveNetwork()?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
    
    /**
     * Converts DownloadManager error code to user-friendly message
     */
    fun getDownloadErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Storage device not found"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            DownloadManager.ERROR_FILE_ERROR -> "File error"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "Network data error"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient storage space"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP error"
            DownloadManager.ERROR_UNKNOWN -> "Unknown download error"
            else -> "Download failed (Error code: $statusCode)"
        }
    }
    
    /**
     * Determines if the error allows retry
     */
    fun isRetryableError(statusCode: Int): Boolean {
        return when (statusCode) {
            DownloadManager.ERROR_HTTP_DATA_ERROR,
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE,
            DownloadManager.ERROR_TOO_MANY_REDIRECTS,
            DownloadManager.ERROR_UNKNOWN -> true
            else -> false
        }
    }
    
    /**
     * Determines maximum retry attempts based on error type
     */
    fun getMaxRetryAttempts(statusCode: Int): Int {
        return when (statusCode) {
            DownloadManager.ERROR_HTTP_DATA_ERROR -> 3
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> 2
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> 1
            else -> 1
        }
    }
    
    /**
     * Calculate exponential backoff delay for retries
     */
    fun getRetryDelayMs(attempt: Int): Long {
        return when {
            attempt <= 0 -> 1000
            attempt == 1 -> 5000
            attempt == 2 -> 15000
            else -> 60000 // Max 1 minute
        }
    }
}
