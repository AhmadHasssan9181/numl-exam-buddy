package com.noobdev.numlexambuddy.Screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noobdev.numlexambuddy.data.DriveServiceManager
import kotlinx.coroutines.launch

@Composable
fun TestDriveScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var testResult by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var validationStatus by remember { mutableStateOf<String?>(null) }
    var isValidating by remember { mutableStateOf(false) }
    
    // Log when the screen is initialized
    LaunchedEffect(key1 = Unit) {
        Log.d("TestDriveScreen", "TestDriveScreen initialized")
        // Automatically validate credentials on screen load
        isValidating = true
        try {
            val driveServiceManager = DriveServiceManager(context)
            val (isValid, error) = driveServiceManager.validateCredentials()
            validationStatus = if (isValid) {
                "✅ Service account credentials are valid"
            } else {
                "❌ Service account credentials issue: $error"
            }
        } catch (e: Exception) {
            Log.e("TestDriveScreen", "Error validating credentials", e)
            validationStatus = "❌ Error validating credentials: ${e.message}"
        } finally {
            isValidating = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Test Google Drive API",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Credential validation status
        if (isValidating) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validating credentials...")
            }
        } else {
            validationStatus?.let { status ->
                Surface(
                    color = if (status.startsWith("✅")) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = status,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = if (status.startsWith("✅"))
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
          Spacer(modifier = Modifier.height(16.dp))
        
        // Add a button to specifically re-validate credentials
        OutlinedButton(
            onClick = {
                isValidating = true
                validationStatus = null
                errorMessage = null
                testResult = emptyList()
                
                scope.launch {
                    try {
                        val driveServiceManager = DriveServiceManager(context)
                        val (isValid, error) = driveServiceManager.validateCredentials()
                        validationStatus = if (isValid) {
                            "✅ Service account credentials are valid"
                        } else {
                            "❌ Service account credentials issue: $error"
                        }
                    } catch (e: Exception) {
                        Log.e("TestDriveScreen", "Error validating credentials", e)
                        validationStatus = "❌ Error validating credentials: ${e.message}"
                    } finally {
                        isValidating = false
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !isLoading && !isValidating
        ) {
            Text("Validate Credentials")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Test connection button
        Button(
            onClick = {
                Log.d("TestDriveScreen", "Starting Drive connection test")
                isLoading = true
                errorMessage = null
                testResult = emptyList()
                
                scope.launch {
                    try {
                        val driveServiceManager = DriveServiceManager(context)
                        val rootFolderId = "1wnOHvbCCBYTOLvfL6UJjzQdenkQ4Bk8r"  // Your root folder ID
                        
                        // Use the new testConnection method
                        val result = driveServiceManager.testConnection(rootFolderId)
                        
                        result.fold(
                            onSuccess = { folders ->
                                testResult = folders
                            },
                            onFailure = { error ->
                                Log.e("TestDriveScreen", "Connection test failed", error)
                                errorMessage = "Error: ${error.message}\nCause: ${error.cause?.message ?: "Unknown"}"
                            }
                        )
                    } catch (e: Exception) {
                        Log.e("TestDriveScreen", "Error during Drive test", e)
                        errorMessage = "Error: ${e.message}\nCause: ${e.cause?.message ?: "Unknown"}"
                    } finally {
                        isLoading = false
                        Log.d("TestDriveScreen", "Drive test completed")
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !isLoading && !isValidating
        ) {
            Text("Test Drive Connection")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Testing connection to Google Drive...",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (errorMessage != null) {
            Column {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Connection Failed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Add a help section to assist the user
                Text(
                    text = "Troubleshooting Tips:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("1. Check your internet connection")
                        Text("2. Verify that the service account JSON file is properly formatted")
                        Text("3. Confirm that the service account has access to the Google Drive folder")
                        Text("4. Make sure the root folder ID is correct")
                        Text("5. Enable Google Drive API in Google Cloud Console")
                    }
                }
            }
        } else if (testResult.isNotEmpty()) {
            Text(
                text = "Connection successful! Found:",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn {
                items(testResult) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
