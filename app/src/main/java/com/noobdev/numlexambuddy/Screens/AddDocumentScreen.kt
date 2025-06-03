package com.noobdev.numlexambuddy.Screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noobdev.numlexambuddy.utils.FileStorageUtils
import com.noobdev.numlexambuddy.utils.MimeTypeUtils
import com.noobdev.numlexambuddy.viewmodel.DocumentViewModel
import kotlinx.coroutines.launch

/**
 * Screen for adding a new document.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    viewModel: DocumentViewModel,
    onNavigateBack: () -> Unit,
    onDocumentAdded: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedDocument by viewModel.selectedDocument.collectAsState()
    
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var documentTitle by remember { mutableStateOf("") }
    var documentSubject by remember { mutableStateOf("") }
    var documentSemester by remember { mutableStateOf("") }
    var documentDepartment by remember { mutableStateOf("") }
    var documentTags by remember { mutableStateOf("") }
    var documentUrl by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    
    // File picker launcher
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = MimeTypeUtils.getFileNameFromUri(context, it) ?: "Unknown file"
            documentTitle = selectedFileName // Set title to file name by default
        }
    }
    
    // Check if a document was added
    LaunchedEffect(selectedDocument) {
        if (selectedDocument != null) {
            // Navigate to document detail screen
            onDocumentAdded(selectedDocument!!.id)
        }
    }
    
    // Show error message in snackbar
    LaunchedEffect(error) {
        error?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Document") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Import options
                    Text(
                        text = "Import Document",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // File picker card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.height(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = if (selectedFileUri == null) 
                                    "Select a document from your device" 
                                else 
                                    "Selected: $selectedFileName",
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { pickFileLauncher.launch("*/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Browse Files")
                            }
                        }
                    }
                    
                    Divider()
                    
                    // Document metadata
                    Text(
                        text = "Document Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = documentTitle,
                        onValueChange = { documentTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = documentSubject,
                        onValueChange = { documentSubject = it },
                        label = { Text("Subject (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = documentSemester,
                            onValueChange = { documentSemester = it },
                            label = { Text("Semester") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = documentDepartment,
                            onValueChange = { documentDepartment = it },
                            label = { Text("Department") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    OutlinedTextField(
                        value = documentTags,
                        onValueChange = { documentTags = it },
                        label = { Text("Tags (Comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = {
                                selectedFileUri?.let { uri ->
                                    val semester = documentSemester.toIntOrNull()
                                    val tags = documentTags.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                    
                                    val mimeType = MimeTypeUtils.getMimeTypeFromUri(context, uri) ?: "*/*"
                                    val sourceUrl = uri.toString()
                                    
                                    viewModel.importDocument(
                                        uri = uri,
                                        fileName = documentTitle,
                                        mimeType = mimeType,
                                        sourceUrl = sourceUrl,
                                        subject = documentSubject.ifBlank { null },
                                        semester = semester,
                                        department = documentDepartment.ifBlank { null },
                                        tags = tags
                                    )
                                } ?: run {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please select a file first")
                                    }
                                }
                            },
                            enabled = selectedFileUri != null && documentTitle.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add Document")
                        }
                    }
                    
                    // Space at the bottom
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun AddDocumentScreenPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text("Add Document Screen Preview")
    }
}
