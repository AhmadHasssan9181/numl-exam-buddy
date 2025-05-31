package com.noobdev.numlexambuddy.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.UnknownHostException

class DriveServiceManager(private val context: Context) {
    private val TAG = "DriveServiceManager"
    private val scopes = listOf(DriveScopes.DRIVE_READONLY)
    
    /**
     * Validates if the service account credentials can be loaded properly
     * Returns a pair with (isValid, errorMessage)
     */
    suspend fun validateCredentials(): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Validating service account credentials")
            val resourceId = context.resources.getIdentifier("service_account", "raw", context.packageName)
            if (resourceId == 0) {
                Log.e(TAG, "Service account file not found in resources")
                return@withContext Pair(false, "Service account file not found in resources")
            }
            
            val credentialsStream: InputStream = context.resources.openRawResource(resourceId)
            
            try {
                val credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(scopes)
                
                // Try to get an access token to confirm the credentials work
                credentials.refreshIfExpired()
                
                Log.d(TAG, "Credentials validation successful")
                return@withContext Pair(true, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error validating credentials: ${e.message}", e)
                return@withContext Pair(false, "Invalid service account: ${e.message}")
            } finally {
                credentialsStream.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during credential validation: ${e.message}", e)
            return@withContext Pair(false, "Error validating credentials: ${e.message}")
        }
    }
    
    suspend fun getDriveService(): Drive = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing Google Drive service")
            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            
            // Load service account credentials from the raw resource
            val resourceId = context.resources.getIdentifier("service_account", "raw", context.packageName)
            if (resourceId == 0) {
                Log.e(TAG, "Service account file not found in resources")
                throw Exception("Service account file not found in resources")
            }
            
            Log.d(TAG, "Service account resource found with ID: $resourceId")
            val credentialsStream: InputStream = context.resources.openRawResource(resourceId)
            
            try {
                Log.d(TAG, "Loading credentials from stream")
                val credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(scopes)
                
                Log.d(TAG, "Successfully created credentials with scopes: $scopes")
                Log.d(TAG, "Building Drive service")
                
                return@withContext Drive.Builder(
                    transport, 
                    jsonFactory,
                    HttpCredentialsAdapter(credentials)
                )
                .setApplicationName("NUML Exam Buddy")
                .build()
                .also { Log.d(TAG, "Drive service successfully built") }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse credentials: ${e.message}", e)
                throw Exception("Failed to parse credentials: ${e.message}", e)
            } finally {
                credentialsStream.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Drive service", e)
            throw e
        }
    }
    
    /**
     * Check if the device has an active internet connection
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Tests the connection to Google Drive and returns the result
     */
    suspend fun testConnection(rootFolderId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Testing connection with root folder ID: $rootFolderId")
            
            // First check if network is available
            if (!isNetworkAvailable()) {
                Log.e(TAG, "No internet connection available")
                return@withContext Result.failure(Exception("No internet connection. Please check your network settings."))
            }
            
            // Then validate credentials
            val (isValid, errorMessage) = validateCredentials()
            if (!isValid) {
                Log.e(TAG, "Credential validation failed: $errorMessage")
                return@withContext Result.failure(Exception(errorMessage))
            }
            
            val driveService = getDriveService()
            
            try {
                val result = driveService.files().list()
                    .setQ("'$rootFolderId' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false")
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute()
                    
                val folders = result.files.map { 
                    "Folder: ${it.name} (ID: ${it.id})"
                }
                
                if (folders.isEmpty()) {
                    Log.d(TAG, "No folders found in root directory")
                    return@withContext Result.success(listOf("No folders found in the root directory."))
                }
                
                Log.d(TAG, "Found ${folders.size} folders in root directory")
                return@withContext Result.success(folders)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Network error: Unable to reach Google servers", e)
                return@withContext Result.failure(Exception("Network error: Unable to reach Google servers. Check your internet connection."))
            } catch (e: Exception) {
                Log.e(TAG, "Error executing Drive API call: ${e.message}", e)
                return@withContext Result.failure(e)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error testing connection: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
}