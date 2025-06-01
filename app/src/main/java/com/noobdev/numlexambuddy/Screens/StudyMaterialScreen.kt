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
import androidx.compose.material.icons.automirrored.outlined.MenuBook
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
import com.noobdev.numlexambuddy.viewmodel.StudyMaterialViewModel
import com.noobdev.numlexambuddy.viewmodel.ViewModelFactory
import com.noobdev.numlexambuddy.model.Department
import com.noobdev.numlexambuddy.model.StudyMaterial
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMaterialScreen(
    viewModel: StudyMaterialViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current)
    ),
    onBackClick: () -> Unit = {}
) {
    // Color definitions matching MainScreen
    val primaryColor = Color(0xFF1E88E5) // Vibrant blue
    val accentColor = Color(0xFFFF9800) // Orange
    val backgroundGray = Color(0xFFF5F5F5) // Light gray background
    val cardBackgroundColor = Color(0xFFE8F5E9) // Light green background
    val textPrimaryColor = Color(0xFF212121) // Dark text

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
    val studyMaterials = uiState.studyMaterials
    val loading = uiState.isLoading
    val error = uiState.error

    // Filter study materials by search query
    val filteredStudyMaterials = remember(studyMaterials, searchQuery) {
        studyMaterials.filter { material ->
            searchQuery.isEmpty() ||
                    material.title.contains(searchQuery, ignoreCase = true) ||
                    material.subject.contains(searchQuery, ignoreCase = true)
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
            viewModel.loadStudyMaterials(
                selectedDepartment!!.code,
                "SEMESTER-${selectedSemester!!}",
                selectedSubject!!
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGray)
            .verticalScroll(rememberScrollState())
    ) {
        // Header section matching MainScreen style
        StudyMaterialHeaderSection(
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
            StudyMaterialSearchSection(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onClose = { isSearchActive = false },
                resultCount = filteredStudyMaterials.size,
                accentColor = accentColor,
                textColor = textPrimaryColor
            )
        }

        // Filters section
        StudyMaterialFiltersSection(
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
        StudyMaterialResultsSection(
            loading = loading,
            error = error,
            materials = filteredStudyMaterials,
            selectedDepartment = selectedDepartment,
            selectedSemester = selectedSemester,
            selectedSubject = selectedSubject,
            onMaterialDownload = { material ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.downloadStudyMaterial(material)
            },
            primaryColor = primaryColor,
            accentColor = accentColor,
            textColor = textPrimaryColor
        )
    }
}

/**
 * Header section matching MainScreen design
 */
@Composable
fun StudyMaterialHeaderSection(
    primaryColor: Color,
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
                    color = Color.White.copy(alpha = 0.9f)
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
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search materials",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title text
            Text(
                text = "📚 Study Materials",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                )
            )

            Text(
                text = "Comprehensive study resources and notes",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f)
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
fun StudyMaterialSearchSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClose: () -> Unit,
    resultCount: Int,
    accentColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            "🔍 Search materials or subjects...",
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
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(
                            Color.Gray.copy(alpha = 0.1f),
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
                    else Color.Red.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (resultCount > 0) "📖 Found $resultCount materials"
                        else "❌ No materials found",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (resultCount > 0) accentColor else Color.Red
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
fun StudyMaterialFiltersSection(
    departments: List<Department>,
    selectedDepartment: Department?,
    selectedSemester: Int?,
    selectedSubject: String?,
    subjects: List<String>,
    semArray: List<Int>,
    onDepartmentSelect: (Department) -> Unit,
    onSemesterSelect: (Int) -> Unit,
    onSubjectSelect: (String) -> Unit,
    primaryColor: Color,
    accentColor: Color,
    cardBackgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
            StudyMaterialFilterCategory(
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
                        StudyMaterialDepartmentChip(
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
            StudyMaterialFilterCategory(
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
                        StudyMaterialSemesterChip(
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
                StudyMaterialFilterCategory(
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
                            StudyMaterialSubjectChip(
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
fun StudyMaterialFilterCategory(
    title: String,
    isSelected: Boolean,
    textColor: Color,
    accentColor: Color,
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
fun StudyMaterialResultsSection(
    loading: Boolean,
    error: String?,
    materials: List<StudyMaterial>,
    selectedDepartment: Department?,
    selectedSemester: Int?,
    selectedSubject: String?,
    onMaterialDownload: (StudyMaterial) -> Unit,
    primaryColor: Color,
    accentColor: Color,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        when {
            loading -> {
                StudyMaterialLoadingState(primaryColor = primaryColor, textColor = textColor)
            }
            error != null -> {
                StudyMaterialErrorState(error = error, textColor = textColor)
            }
            materials.isEmpty() && selectedDepartment != null && selectedSemester != null && selectedSubject != null -> {
                StudyMaterialEmptyState(primaryColor = primaryColor, textColor = textColor)
            }
            selectedDepartment == null || selectedSemester == null || selectedSubject == null -> {
                StudyMaterialWelcomeState(primaryColor = primaryColor, textColor = textColor)
            }
            else -> {
                StudyMaterialsList(
                    materials = materials,
                    onDownload = onMaterialDownload,
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
fun StudyMaterialLoadingState(primaryColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                text = "Loading Study Materials...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = "Fetching the latest study resources",
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
fun StudyMaterialErrorState(error: String, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something Went Wrong",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
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
fun StudyMaterialEmptyState(primaryColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Study Materials Found",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = "No study materials available for this subject",
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
fun StudyMaterialWelcomeState(primaryColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment =Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.LibraryBooks,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ready to Study? 📚",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = "Use the filters above to find study materials for your courses. Start by selecting your department!",
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
 * Study materials list with MainScreen styling
 */
@Composable
fun StudyMaterialsList(
    materials: List<StudyMaterial>,
    onDownload: (StudyMaterial) -> Unit,
    accentColor: Color,
    textColor: Color
) {
    // Results header
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Available Study Materials",
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
                    text = "${materials.size}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Materials list
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(400.dp) // Fixed height to prevent overflow
    ) {
        items(materials) { material ->
            StudyMaterialCard(
                material = material,
                onDownloadClick = { onDownload(material) },
                accentColor = accentColor,
                textColor = textColor
            )
        }
    }
}

/**
 * Study material card with MainScreen styling
 */
@Composable
fun StudyMaterialCard(
    material: StudyMaterial,
    onDownloadClick: () -> Unit,
    accentColor: Color,
    textColor: Color
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        text = material.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = material.subject,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor.copy(alpha = 0.7f)
                        )
                    )
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
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Gray.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${material.type} • Semester ${material.semester}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (material.size.isNotEmpty()) {
                    Text(
                        text = material.size,
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
fun StudyMaterialDepartmentChip(
    department: Department,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    accentColor: Color,
    cardBackgroundColor: Color,
    textColor: Color
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
            containerColor = if (isSelected) cardBackgroundColor else Color.White
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
                color = if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f)
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
fun StudyMaterialSemesterChip(
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
                color = if (isSelected) accentColor else Color.Gray.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$semester",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else textColor
            )
        )
    }
}

/**
 * Subject chip with MainScreen styling
 */
@Composable
fun StudyMaterialSubjectChip(
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
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
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