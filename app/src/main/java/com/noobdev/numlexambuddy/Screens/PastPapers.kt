package com.noobdev.numlexambuddy.Screens

        import androidx.compose.animation.AnimatedVisibility
        import androidx.compose.runtime.collectAsState
        import androidx.compose.animation.core.Spring
        import androidx.compose.animation.core.animateFloatAsState
        import androidx.compose.animation.core.spring
        import androidx.compose.animation.core.tween
        import androidx.compose.animation.fadeIn
        import androidx.compose.animation.fadeOut
        import androidx.compose.foundation.background
        import androidx.compose.foundation.border
        import androidx.compose.foundation.clickable
        import androidx.compose.foundation.layout.*
        import androidx.compose.foundation.lazy.LazyColumn
        import androidx.compose.foundation.lazy.LazyRow
        import androidx.compose.foundation.lazy.items
        import androidx.compose.foundation.shape.CircleShape
        import androidx.compose.foundation.shape.RoundedCornerShape
        import androidx.compose.material.icons.Icons
        import androidx.compose.material.icons.filled.ArrowDropDown
        import androidx.compose.material.icons.filled.Search
        import androidx.compose.material.icons.automirrored.outlined.ArrowBack
        import androidx.compose.material.icons.rounded.FilterList
        import androidx.compose.material.icons.rounded.History
        import androidx.compose.material.icons.rounded.School
        import androidx.compose.material3.*
        import androidx.compose.runtime.*
        import androidx.compose.ui.Alignment
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.draw.alpha
        import androidx.compose.ui.draw.clip
        import androidx.compose.ui.draw.scale
        import androidx.compose.ui.graphics.Brush
        import androidx.compose.ui.graphics.Color
        import androidx.compose.ui.graphics.vector.ImageVector
        import androidx.compose.ui.text.font.FontWeight
        import androidx.compose.ui.text.style.TextAlign
        import androidx.compose.ui.tooling.preview.Preview
        import androidx.compose.ui.unit.dp
        import androidx.compose.ui.unit.sp
        import com.noobdev.numlexambuddy.ui.theme.NumlExamBuddyTheme
        import kotlinx.coroutines.delay
        import androidx.lifecycle.viewmodel.compose.viewModel
        import androidx.compose.ui.platform.LocalContext
        import androidx.compose.material3.CircularProgressIndicator
        import com.noobdev.numlexambuddy.viewmodel.PastPapersViewModel
        import com.noobdev.numlexambuddy.viewmodel.PastPapersViewModelFactory

        data class Department(val name: String, val code: String)
        data class Paper(
            val title: String,
            val subject: String,
            val semester: Int,
            val year: String,
            val department: String,
            val fileId: String = "",
            val downloadUrl: String = ""
        )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastPapersScreen(
    viewModel: PastPapersViewModel = viewModel(
        factory = PastPapersViewModelFactory(LocalContext.current)
    ),
    onBack: () -> Unit = {}
) {
    val departments = listOf(
        Department("Computer Science", "BSCS"),
        Department("Artificial Intelligence", "BSAI"),
        Department("Information Technology", "BSIT"),
        Department("Software Engineering", "BSSE"),
        Department("Associate Degree in Computing", "ADC")
    )

    var selectedDepartment by remember { mutableStateOf<Department?>(null) }
    var selectedSemester by remember { mutableStateOf<Int?>(null) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    val semArray by remember { mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 7, 8)) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Get values from ViewModel
    val subjects by viewModel.subjects.collectAsState()
    val papers by viewModel.papers.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Filter papers by search query
    val filteredPapers = remember(papers, searchQuery) {
        papers.filter { paper ->
            searchQuery.isEmpty() ||
                    paper.title.contains(searchQuery, ignoreCase = true) ||
                    paper.subject.contains(searchQuery, ignoreCase = true)
        }
    }   
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Past Papers",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search bar with animation
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search by subject or exam type") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Filters section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Title with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Rounded.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Filters",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Department selection
                Text(
                    "Department",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(departments) { department ->
                        DepartmentChip(
                            department = department,
                            selected = department == selectedDepartment,
                            onSelect = {
                                selectedDepartment = if (selectedDepartment == department) null else department
                                if (selectedDepartment != null) {
                                    viewModel.selectDegree(selectedDepartment!!.code)
                                }
                                // Reset semester and subject when department changes
                                selectedSemester = null
                                selectedSubject = null
                            }
                        )
                    }
                }

                // Semester selection
                Text(
                    "Semester",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(semArray) { semester ->
                        SemesterChip(
                            semester = semester,
                            selected = semester == selectedSemester,
                            onSelect = {
                                selectedSemester = if (selectedSemester == semester) null else semester

                                // Only call viewModel if both department and semester are selected
                                if (selectedDepartment != null && selectedSemester != null) {
                                    viewModel.selectSemester(selectedDepartment!!.code, selectedSemester!!)
                                }

                                // Reset subject when semester changes
                                selectedSubject = null
                            }
                        )
                    }
                }

                // Subject selection - only show if we have subjects
                if (subjects.isNotEmpty()) {
                    Text(
                        "Subject",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjects) { subject ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        selectedSubject = if (selectedSubject == subject) null else subject

                                        if (selectedDepartment != null && selectedSemester != null && selectedSubject != null) {
                                            viewModel.selectSubject(selectedDepartment!!.code, selectedSemester!!, selectedSubject!!)
                                        }
                                    },
                                color = if (subject == selectedSubject)
                                    MaterialTheme.colorScheme.tertiaryContainer
                                else
                                    MaterialTheme.colorScheme.surface,
                                shadowElevation = if (subject == selectedSubject) 2.dp else 0.dp
                            ) {
                                Text(
                                    text = subject,
                                    color = if (subject == selectedSubject)
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (subject == selectedSubject) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Loading indicator
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error message
            error?.let { errorMessage ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                // Clear error after showing it
                LaunchedEffect(errorMessage) {
                    delay(3000)
                    if (viewModel.error.value == errorMessage) {
                        viewModel.clearError()
                    }
                }
            }

            // Results section
            if (!loading && error == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Results header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Available Papers",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            "${filteredPapers.size} results",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // Paper list
                    if (filteredPapers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No papers found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Try adjusting your filters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredPapers) { paper ->
                                PaperCard(
                                    paper = paper,
                                    onDownload = { viewModel.downloadPaper(paper) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

        @Composable
        fun DepartmentChip(
            department: Department,
            selected: Boolean,
            onSelect: () -> Unit
        ) {
            val backgroundColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface

            val textColor = if (selected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

            val scale by animateFloatAsState(
                targetValue = if (selected) 1.05f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )

            Surface(
                modifier = Modifier
                    .scale(scale)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelect() },
                color = backgroundColor,
                shadowElevation = if (selected) 2.dp else 0.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = department.name,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        @Composable
        fun SemesterChip(
            semester: Int,
            selected: Boolean,
            onSelect: () -> Unit
        ) {
            val backgroundColor = if (selected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface

            val textColor = if (selected)
                MaterialTheme.colorScheme.onSecondaryContainer
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable { onSelect() }
                    .border(
                        width = if (selected) 0.dp else 1.dp,
                        color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$semester",
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

@Composable
fun PaperCard(
    paper: Paper,
    onDownload: () -> Unit = {}
) {
    var isHovered by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isHovered = !isHovered },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            hoveredElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = paper.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = paper.year,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = paper.subject,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Semester ${paper.semester}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = paper.department,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

            AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                Button(
                    onClick = onDownload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Download Paper")
                }
            }
        }
    }
}        @Preview(showBackground = true)
        @Composable
        fun PastPapersScreenPreview() {
            NumlExamBuddyTheme {
                PastPapersScreen(
                    onBack = {}
                )
            }
        }