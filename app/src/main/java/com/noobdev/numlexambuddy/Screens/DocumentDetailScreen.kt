package com.noobdev.numlexambuddy.Screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noobdev.numlexambuddy.model.Document
import com.noobdev.numlexambuddy.model.DocumentContent
import com.noobdev.numlexambuddy.utils.FileStorageUtils
import com.noobdev.numlexambuddy.viewmodel.DocumentViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen for displaying document details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    viewModel: DocumentViewModel,
    documentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (Document) -> Unit,
    onNavigateToEdit: (Document) -> Unit
) {
    val selectedDocument by viewModel.selectedDocument.collectAsState()
    val documentContent by viewModel.documentContent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load the document when the screen is first shown
    LaunchedEffect(documentId) {
        viewModel.selectDocument(
            selectedDocument ?: run {
                // If the document is not already selected, fetch it
                val document = viewModel.documents.value.find { it.id == documentId }
                document?.also { viewModel.selectDocument(it) }
                document
            } ?: return@LaunchedEffect
        )
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
                title = {
                    Text(
                        text = selectedDocument?.title ?: "Document Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        selectedDocument?.let { document ->
                            val file = File(document.filePath)
                            if (file.exists()) {
                                FileStorageUtils.shareDocument(context, file, document.mimeType)
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("File not found")
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }

                    IconButton(onClick = {
                        selectedDocument?.let { onNavigateToEdit(it) }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        floatingActionButton = {
            selectedDocument?.let { document ->
                FloatingActionButton(onClick = { onNavigateToChat(document) }) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat about document")
                }
            }
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
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            } else {
                selectedDocument?.let { document ->
                    DocumentDetailContent(
                        document = document,
                        content = documentContent,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    // No document selected
                    Text(
                        text = "Document not found",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Document") },
                text = { Text("Are you sure you want to delete this document? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedDocument?.let {
                                viewModel.deleteDocument(it)
                                onNavigateBack()
                            }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Content for the document detail screen.
 */
@Composable
fun DocumentDetailContent(
    document: Document,
    content: Any?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Document metadata card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getDocumentTypeIcon(document.documentType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = document.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        document.subject?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // File details
                DocumentMetadataRow("Type", document.documentType.toString())
                DocumentMetadataRow("Size", FileStorageUtils.formatFileSize(document.size))
                DocumentMetadataRow("Downloaded", formatDateWithTime(document.downloadDate))
                document.lastAccessed?.let {
                    DocumentMetadataRow("Last opened", formatDateWithTime(it))
                }
                document.department?.let {
                    DocumentMetadataRow("Department", it)
                }
                document.semester?.let {
                    DocumentMetadataRow("Semester", it.toString())
                }
                if (document.tags.isNotEmpty()) {
                    DocumentMetadataRow("Tags", document.tags.joinToString(", "))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Document summary if available
        document.summary?.let { summary ->
            if (summary.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Document preview card (text content)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Document Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    content is DocumentContent && content.content.isNotBlank() -> {
                        Text(
                            text = content.content.take(2000),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (content.content.length > 2000) {
                            Text(
                                text = "... (content truncated)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    content is List<*> && content.isNotEmpty() -> {
                        // Handle case where content is a list of content chunks
                        val firstChunk = content.first().toString()
                        Text(
                            text = if (firstChunk.length > 2000) firstChunk.substring(0, 2000) else firstChunk,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (firstChunk.length > 2000) {
                            Text(
                                text = "... (content truncated)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    content != null -> {
                        Text(
                            text = "No preview available. Try opening the document.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Text(
                            text = "Loading document content...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    // Open document using an external app

                    val file = File(document.filePath)
                    if (file.exists()) {
                        FileStorageUtils.openDocument(context, file, document.mimeType)
                    }
                }
            ) {
                Text("Open Document")
            }

            OutlinedButton(
                onClick = {
                    // Share document

                    val file = File(document.filePath)
                    if (file.exists()) {
                        FileStorageUtils.shareDocument(context, file, document.mimeType)
                    }
                }
            ) {
                Text("Share")
            }
        }

        // Space at the bottom to avoid FAB overlap
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Row for displaying document metadata.
 */
@Composable
fun DocumentMetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Format a date with time as a string.
 */
fun formatDateWithTime(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return formatter.format(date)
}

/**
 * Preview for DocumentDetailScreen.
 */
@Preview
@Composable
fun DocumentDetailScreenPreview() {
    // This preview would require mock data and viewmodel
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text("Document Detail Preview")
    }
}
