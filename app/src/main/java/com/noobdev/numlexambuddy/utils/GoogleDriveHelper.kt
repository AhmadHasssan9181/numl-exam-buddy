package com.noobdev.numlexambuddy.utils

import android.content.Context
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.InputStream

/**
 * Helper class for creating Google Drive service instances
 */
object GoogleDriveHelper {
    
    /**
     * Creates a Google Drive service instance using service account credentials
     */
    fun createDriveService(context: Context): Drive {
        // Load service account credentials from resources
        val resourceId = context.resources.getIdentifier("service_account", "raw", context.packageName)
        if (resourceId == 0) {
            throw IllegalStateException("Service account file not found in resources")
        }
        
        val credentialsStream: InputStream = context.resources.openRawResource(resourceId)
        
        val credentials = GoogleCredentials.fromStream(credentialsStream)
            .createScoped(listOf(DriveScopes.DRIVE_READONLY))
        
        credentialsStream.close()
        
        // Build and return the Drive service
        return Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        )
            .setApplicationName("NUML Exam Buddy")
            .build()
    }
}
