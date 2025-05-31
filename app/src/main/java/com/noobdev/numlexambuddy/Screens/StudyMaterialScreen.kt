package com.noobdev.numlexambuddy.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noobdev.numlexambuddy.viewmodel.StudyMaterialViewModel
import com.noobdev.numlexambuddy.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMaterialScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: StudyMaterialViewModel = viewModel(
        factory = ViewModelFactory(context)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf<String?>(null) }
    var selectedSemester by remember { mutableStateOf<String?>(null) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }    // Filter study materials based on search text
    val filteredStudyMaterials = remember(uiState.studyMaterials, searchText) {
        if (searchText.isBlank()) {
            uiState.studyMaterials
        } else {
            uiState.studyMaterials.filter { studyMaterial ->
                studyMaterial.title.contains(searchText, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadDepartments()
    }

    LaunchedEffect(selectedDepartment) {
        selectedDepartment?.let { department ->
            viewModel.loadSemesters(department)
            selectedSemester = null
            selectedSubject = null
        }
    }

    LaunchedEffect(selectedDepartment, selectedSemester) {
        if (selectedDepartment != null && selectedSemester != null) {
            viewModel.loadSubjects(selectedDepartment!!, selectedSemester!!)
            selectedSubject = null
        }
    }

    LaunchedEffect(selectedDepartment, selectedSemester, selectedSubject) {
        if (selectedDepartment != null && selectedSemester != null && selectedSubject != null) {
            viewModel.loadStudyMaterials(selectedDepartment!!, selectedSemester!!, selectedSubject!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Study Material") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6200EE),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search study materials...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Department Dropdown
                    DropdownMenuBox(
                        label = "Department",
                        selectedValue = selectedDepartment,
                        options = uiState.departments,
                        onSelectionChange = { selectedDepartment = it },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Semester Dropdown
                    DropdownMenuBox(
                        label = "Semester",
                        selectedValue = selectedSemester,
                        options = uiState.semesters,
                        onSelectionChange = { selectedSemester = it },
                        enabled = selectedDepartment != null,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Subject Dropdown
                    DropdownMenuBox(
                        label = "Subject",
                        selectedValue = selectedSubject,
                        options = uiState.subjects,
                        onSelectionChange = { selectedSubject = it },
                        enabled = selectedDepartment != null && selectedSemester != null
                    )
                }
            }

            // Content Area
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6200EE))
                    }
                }
                
                uiState.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                selectedDepartment == null -> {
                    EmptyStateMessage("Please select a department to view study materials")
                }
                
                selectedSemester == null -> {
                    EmptyStateMessage("Please select a semester to view study materials")
                }
                
                selectedSubject == null -> {
                    EmptyStateMessage("Please select a subject to view study materials")
                }
                
                filteredStudyMaterials.isEmpty() -> {
                    EmptyStateMessage(
                        if (searchText.isNotBlank()) {
                            "No study materials found matching \"$searchText\""
                        } else {
                            "No study materials available for the selected filters"
                        }
                    )
                }
                  else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredStudyMaterials) { studyMaterial ->
                            StudyMaterialCard(
                                studyMaterial = studyMaterial,
                                onDownloadClick = { viewModel.downloadStudyMaterial(studyMaterial) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyMaterialCard(
    studyMaterial: StudyMaterial,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDownloadClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File Type Icon
            Icon(
                imageVector = when {
                    studyMaterial.title.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
                    else -> Icons.Default.Description
                },
                contentDescription = "File type",
                tint = when {
                    studyMaterial.title.endsWith(".pdf", ignoreCase = true) -> Color(0xFFD32F2F)
                    else -> Color(0xFF1976D2)
                },
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = studyMaterial.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!studyMaterial.size.isNullOrBlank()) {
                    Text(
                        text = "Size: ${studyMaterial.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Download Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF6200EE))
                    .clickable { onDownloadClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↓",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenuBox(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onSelectionChange: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && enabled },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
