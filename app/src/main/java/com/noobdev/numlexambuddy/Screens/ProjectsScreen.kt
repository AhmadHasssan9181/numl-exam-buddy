package com.noobdev.numlexambuddy.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noobdev.numlexambuddy.viewmodel.ProjectsViewModel
import com.noobdev.numlexambuddy.viewmodel.ViewModelFactory
import com.noobdev.numlexambuddy.model.Department
import com.noobdev.numlexambuddy.model.Project
import com.noobdev.numlexambuddy.components.BottomNavBar
import com.noobdev.numlexambuddy.ui.theme.customColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current)
    ),
    onBackClick: () -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    onNavigateToPastPapers: () -> Unit = {},
    onNavigateToLectures: () -> Unit = {},
    onNavigateToStudyMaterial: () -> Unit = {}
) {
    // Use theme colors instead of hardcoded colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.customColors.accentAmber
    val backgroundGray = MaterialTheme.colorScheme.background
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val textPrimaryColor = MaterialTheme.colorScheme.onBackground

    val haptic = LocalHapticFeedback.current

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
    val uiState by viewModel.uiState.collectAsState()
    val subjects = uiState.subjects
    val projects = uiState.projects
    val loading = uiState.isLoading
    val error = uiState.error

    // Filter projects by search query
    val filteredProjects = remember(projects, searchQuery) {
        projects.filter { project ->
            searchQuery.isEmpty() ||
                    project.name.contains(searchQuery, ignoreCase = true) ||
                    (project.subject.isNotEmpty() && project.subject.contains(searchQuery, ignoreCase = true))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadDepartments()
    }
    LaunchedEffect(selectedDepartment) {
        selectedDepartment?.let { department ->
            viewModel.loadSemesters(department.code)
            selectedSemester = null
            selectedSubject = null
        }
    }
    LaunchedEffect(selectedSemester) {
        if (selectedDepartment != null && selectedSemester != null) {
            viewModel.loadSubjects(selectedDepartment!!.code, "SEMESTER-${selectedSemester!!}")
            selectedSubject = null
        }
    }
    LaunchedEffect(selectedSubject) {
        if (selectedDepartment != null && selectedSemester != null && selectedSubject != null) {
            viewModel.loadProjects(
                selectedDepartment!!.code,
                "SEMESTER-${selectedSemester!!}",
                selectedSubject!!
            )        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "projects",
                onNavigateToMain = onNavigateToMain,
                onNavigateToPastPapers = onNavigateToPastPapers,
                onNavigateToLectures = onNavigateToLectures,
                onNavigateToStudyMaterial = onNavigateToStudyMaterial
            )
        }
    ) { paddingValues ->        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
        // Header section matching MainScreen style
        ProjectsHeaderSection(
            primaryColor = primaryColor,
            onBack = onBackClick,
            onSearch = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isSearchActive = !isSearchActive
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search section
        AnimatedVisibility(
            visible = isSearchActive,
            enter = expandVertically() + fadeIn()
        ) {
            ProjectsSearchSection(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onClose = { isSearchActive = false },
                resultCount = filteredProjects.size,
                accentColor = accentColor,
                textColor = textPrimaryColor
            )
        }

        // Filters section
        ProjectsFiltersSection(
            departments = departments,
            selectedDepartment = selectedDepartment,
            selectedSemester = selectedSemester,
            selectedSubject = selectedSubject,
            subjects = subjects,
            semArray = semArray,
            onDepartmentSelect = { dept ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedDepartment = if (selectedDepartment == dept) null else dept
                selectedSemester = null
                selectedSubject = null
            },
            onSemesterSelect = { sem ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedSemester = if (selectedSemester == sem) null else sem
                selectedSubject = null
            },
            onSubjectSelect = { subj ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedSubject = if (selectedSubject == subj) null else subj
            },
            primaryColor = primaryColor,
            accentColor = accentColor,
            cardBackgroundColor = cardBackgroundColor,
            textColor = textPrimaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results section
        ProjectsResultsSection(
            loading = loading,
            error = error,
            projects = filteredProjects,
            selectedDepartment = selectedDepartment,
            selectedSemester = selectedSemester,
            selectedSubject = selectedSubject,
            onProjectDownload = { project ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.downloadProject(project)
            },            primaryColor = primaryColor,
            accentColor = accentColor,
            textColor = textPrimaryColor
        )
        }
    }
}

/**
 * Header section matching MainScreen design
 */
@Composable
fun ProjectsHeaderSection(
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onBack: () -> Unit,
    onSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = primaryColor,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(top = 24.dp, bottom = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            // Top bar with back and search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onBack() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Go back",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Search button
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onSearch() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search projects",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title text
            Text(
                text = "🚀 Projects Hub",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                )
            )

            Text(
                text = "Explore student projects and solutions",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

/**
 * Search section with MainScreen styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsSearchSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClose: () -> Unit,
    resultCount: Int,
    accentColor: Color = MaterialTheme.customColors.accentAmber,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "🔍 Search projects or subjects...",
                            color = textColor.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = accentColor
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    Icons.Rounded.Clear,
                                    contentDescription = "Clear search",
                                    tint = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Close search",
                        tint = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            // Results indicator
            if (searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (resultCount > 0) accentColor.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (resultCount > 0) "🚀 Found $resultCount projects"
                        else "❌ No projects found",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (resultCount > 0) accentColor else MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Filters section with MainScreen design
 */
@Composable
fun ProjectsFiltersSection(
    departments: List<Department>,
    selectedDepartment: Department?,
    selectedSemester: Int?,
    selectedSubject: String?,
    subjects: List<String>,
    semArray: List<Int>,
    onDepartmentSelect: (Department) -> Unit,
    onSemesterSelect: (Int) -> Unit,
    onSubjectSelect: (String) -> Unit,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.customColors.accentAmber,
    cardBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Rounded.FilterList,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Filters",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
            }

            // Department selection
            ProjectFilterCategory(
                title = "Department",
                isSelected = selectedDepartment != null,
                textColor = textColor,
                accentColor = accentColor
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(departments) { department ->
                        ProjectDepartmentChip(
                            department = department,
                            isSelected = department == selectedDepartment,
                            onClick = { onDepartmentSelect(department) },
                            primaryColor = primaryColor,
                            accentColor = accentColor,
                            cardBackgroundColor = cardBackgroundColor,
                            textColor = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Semester selection
            ProjectFilterCategory(
                title = "Semester",
                isSelected = selectedSemester != null,
                textColor = textColor,
                accentColor = accentColor
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(semArray) { semester ->
                        ProjectSemesterChip(
                            semester = semester,
                            isSelected = semester == selectedSemester,
                            onClick = { onSemesterSelect(semester) },
                            primaryColor = primaryColor,
                            accentColor = accentColor,
                            textColor = textColor
                        )
                    }
                }
            }

            // Subject selection
            if (subjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                ProjectFilterCategory(
                    title = "Subject",
                    isSelected = selectedSubject != null,
                    textColor = textColor,
                    accentColor = accentColor
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(subjects) { subject ->
                            ProjectSubjectChip(
                                subject = subject,
                                isSelected = subject == selectedSubject,
                                onClick = { onSubjectSelect(subject) },
                                accentColor = accentColor,
                                textColor = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filter category header
 */
@Composable
fun ProjectFilterCategory(
    title: String,
    isSelected: Boolean,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    accentColor: Color = MaterialTheme.customColors.accentAmber,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = accentColor,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(2.dp)
                    )
                }
            }
        }
        content()
    }
}

/**
 * Results section with MainScreen styling
 */
@Composable
fun ProjectsResultsSection(
    loading: Boolean,
    error: String?,
    projects: List<Project>,
    selectedDepartment: Department?,
    selectedSemester: Int?,
    selectedSubject: String?,
    onProjectDownload: (Project) -> Unit,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.customColors.accentAmber,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        when {
            loading -> {
                ProjectsLoadingState(primaryColor = primaryColor, textColor = textColor)
            }
            error != null -> {
                ProjectsErrorState(error = error, textColor = textColor)
            }
            projects.isEmpty() && selectedDepartment != null && selectedSemester != null && selectedSubject != null -> {
                ProjectsEmptyState(primaryColor = primaryColor, textColor = textColor)
            }
            selectedDepartment == null || selectedSemester == null || selectedSubject == null -> {
                ProjectsWelcomeState(primaryColor = primaryColor, textColor = textColor)
            }
            else -> {
                ProjectsList(
                    projects = projects,
                    onDownload = onProjectDownload,
                    accentColor = accentColor,
                    textColor = textColor
                )
            }
        }
    }
}

/**
 * Loading state with MainScreen styling
 */
@Composable
fun ProjectsLoadingState(
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = primaryColor,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading Projects...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = "Fetching the latest student projects",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Error state with MainScreen styling
 */
@Composable
fun ProjectsErrorState(
    error: String,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something Went Wrong",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Empty state with MainScreen styling
 */
@Composable
fun ProjectsEmptyState(
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.FolderOff,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Projects Found",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = "No projects available for this subject",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Welcome state with MainScreen styling
 */
@Composable
fun ProjectsWelcomeState(
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.Science,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ready to Explore? 🚀",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = "Use the filters above to find projects for your studies. Start by selecting your department!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Projects list with MainScreen styling
 */
@Composable
fun ProjectsList(
    projects: List<Project>,
    onDownload: (Project) -> Unit,
    accentColor: Color = MaterialTheme.customColors.accentAmber,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    // Results header
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Available Projects",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                    Text(
                        "Ready for download",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textColor.copy(alpha = 0.7f)
                        )
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${projects.size}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))    // Projects list
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.heightIn(min = 200.dp, max = 800.dp) // Responsive height
    ) {
        items(projects) { project ->
            ProjectCard(
                project = project,
                onDownloadClick = { onDownload(project) },
                accentColor = accentColor,
                textColor = textColor
            )
        }
    }
}

/**
 * Project card with MainScreen styling
 */
@Composable
fun ProjectCard(
    project: Project,
    onDownloadClick: () -> Unit,
    accentColor: Color = MaterialTheme.customColors.accentAmber,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    var isAnimated by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        delay(100)
        isAnimated = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onDownloadClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (project.subject.isNotEmpty()) {
                        Text(
                            text = project.subject,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Icon(
                        Icons.Rounded.FileDownload,
                        contentDescription = "Download",
                        tint = accentColor,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    val projectInfo = if (project.semester > 0) {
                        "${project.type} • Semester ${project.semester}"
                    } else {
                        project.type
                    }
                    Text(
                        text = projectInfo,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (project.size.isNotEmpty()) {
                    Text(
                        text = project.size,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Department chip with MainScreen styling
 */
@Composable
fun ProjectDepartmentChip(
    department: Department,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.customColors.accentAmber,
    cardBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) cardBackgroundColor else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, accentColor.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Text(
                    text = department.code,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) accentColor else textColor
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = department.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Semester chip with MainScreen styling
 */
@Composable
fun ProjectSemesterChip(
    semester: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    accentColor: Color,
    textColor: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .background(
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$semester",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else textColor
            )
        )
    }
}

/**
 * Subject chip with MainScreen styling
 */
@Composable
fun ProjectSubjectChip(
    subject: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    textColor: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = subject,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accentColor else textColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}