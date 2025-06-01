package com.noobdev.numlexambuddy.Screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noobdev.numlexambuddy.ui.theme.*
import com.noobdev.numlexambuddy.viewmodel.PastPapersViewModel
import com.noobdev.numlexambuddy.viewmodel.PastPapersViewModelFactory
import kotlinx.coroutines.delay

// Data classes
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

/**
 * PastPapersScreen with MainScreen design consistency
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastPapersScreen(
    viewModel: PastPapersViewModel = viewModel(
        factory = PastPapersViewModelFactory(LocalContext.current)
    ),
    onBack: () -> Unit = {}
) {
    // Color definitions matching MainScreen
    val primaryColor = Color(0xFF1E88E5) // Vibrant blue
    val accentColor = Color(0xFFFF9800) // Orange
    val backgroundGray = Color(0xFFF5F5F5) // Light gray background
    val cardBackgroundColor = Color(0xFFE8F5E9) // Light green background
    val textPrimaryColor = Color(0xFF212121) // Dark text

    // Animation states
    var searchExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Sample departments
    val departments = remember {
        listOf(
            Department("Computer Science", "BSCS"),
            Department("Artificial Intelligence", "BSAI"),
            Department("Information Technology", "BSIT"),
            Department("Software Engineering", "BSSE"),
            Department("Cyber Security", "BSCY"),
            Department("Data Science", "BSDS")
        )
    }

    // Filter states
    var selectedDepartment by remember { mutableStateOf<Department?>(null) }
    var selectedSemester by remember { mutableStateOf<Int?>(null) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // ViewModel states
    val subjects by viewModel.subjects.collectAsState()
    val papers by viewModel.papers.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Enhanced paper filtering
    val filteredPapers = remember(papers, searchQuery, selectedDepartment, selectedSemester, selectedSubject) {
        papers.filter { paper ->
            val matchesSearch = searchQuery.isEmpty() ||
                    paper.title.contains(searchQuery, ignoreCase = true) ||
                    paper.subject.contains(searchQuery, ignoreCase = true) ||
                    paper.department.contains(searchQuery, ignoreCase = true)
            val matchesDepartment = selectedDepartment == null ||
                    paper.department == selectedDepartment?.code
            val matchesSemester = selectedSemester == null ||
                    paper.semester == selectedSemester
            val matchesSubject = selectedSubject == null ||
                    paper.subject == selectedSubject

            matchesSearch && matchesDepartment && matchesSemester && matchesSubject
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGray)
            .verticalScroll(rememberScrollState())
    ) {
        // Header section matching MainScreen style
        HeaderSection(
            primaryColor = primaryColor,
            onBack = onBack,
            onSearch = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                searchExpanded = !searchExpanded
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search section
        AnimatedVisibility(
            visible = searchExpanded,
            enter = expandVertically() + fadeIn()
        ) {
            SearchSection(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onClose = { searchExpanded = false },
                resultCount = filteredPapers.size,
                accentColor = accentColor,
                textColor = textPrimaryColor
            )
        }

        // Filters section
        FiltersSection(
            departments = departments,
            selectedDepartment = selectedDepartment,
            selectedSemester = selectedSemester,
            selectedSubject = selectedSubject,
            subjects = subjects,
            onDepartmentSelect = { dept ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedDepartment = if (selectedDepartment == dept) null else dept
                if (selectedDepartment != null) {
                    viewModel.selectDegree(selectedDepartment!!.code)
                }
                selectedSemester = null
                selectedSubject = null
            },
            onSemesterSelect = { sem ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedSemester = if (selectedSemester == sem) null else sem
                if (selectedDepartment != null && selectedSemester != null) {
                    viewModel.selectSemester(selectedDepartment!!.code, selectedSemester!!)
                }
                selectedSubject = null
            },
            onSubjectSelect = { subj ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedSubject = if (selectedSubject == subj) null else subj
                if (selectedDepartment != null && selectedSemester != null && selectedSubject != null) {
                    viewModel.selectSubject(selectedDepartment!!.code, selectedSemester!!, selectedSubject!!)
                }
            },
            primaryColor = primaryColor,
            accentColor = accentColor,
            cardBackgroundColor = cardBackgroundColor,
            textColor = textPrimaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results section
        ResultsSection(
            loading = loading,
            error = error,
            papers = filteredPapers,
            selectedDepartment = selectedDepartment,
            selectedSemester = selectedSemester,
            selectedSubject = selectedSubject,
            searchQuery = searchQuery,
            onPaperDownload = { paper ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.downloadPaper(paper)
            },
            onClearError = { viewModel.clearError() },
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
fun HeaderSection(
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
                            Icons.Rounded.Search,
                            contentDescription = "Search papers",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title text
            Text(
                text = "📚 Past Papers",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                )
            )

            Text(
                text = "Ace your exams with previous papers",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f)
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Search section with MainScreen styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSection(
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
                            "🔍 Search papers, subjects...",
                            color = textColor.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
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
                        text = if (resultCount > 0) "📋 Found $resultCount papers"
                        else "❌ No papers found",
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
fun FiltersSection(
    departments: List<Department>,
    selectedDepartment: Department?,
    selectedSemester: Int?,
    selectedSubject: String?,
    subjects: List<String>,
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
                    Icons.Rounded.Tune,
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
            FilterCategory(
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
                        DepartmentChip(
                            department = department,
                            selected = department == selectedDepartment,
                            onSelect = { onDepartmentSelect(department) },
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
            FilterCategory(
                title = "Semester",
                isSelected = selectedSemester != null,
                textColor = textColor,
                accentColor = accentColor
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items((1..8).toList()) { semester ->
                        SemesterChip(
                            semester = semester,
                            selected = semester == selectedSemester,
                            onSelect = { onSemesterSelect(semester) },
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
                FilterCategory(
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
                            SubjectChip(
                                subject = subject,
                                selected = subject == selectedSubject,
                                onSelect = { onSubjectSelect(subject) },
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
fun FilterCategory(
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
 * Department chip with MainScreen styling
 */
@Composable
fun DepartmentChip(
    department: Department,
    selected: Boolean,
    onSelect: () -> Unit,
    primaryColor: Color,
    accentColor: Color,
    cardBackgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) cardBackgroundColor else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 6.dp else 2.dp
        ),
        border = if (selected) BorderStroke(2.dp, accentColor.copy(alpha = 0.5f)) else null
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
                color = if (selected) accentColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f)
            ) {
                Text(
                    text = department.code,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selected) accentColor else textColor
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
fun SemesterChip(
    semester: Int,
    selected: Boolean,
    onSelect: () -> Unit,
    primaryColor: Color,
    accentColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (selected) accentColor else Color.Gray.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$semester",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else textColor
            )
        )
    }
}

/**
 * Subject chip with MainScreen styling
 */
@Composable
fun SubjectChip(
    subject: String,
    selected: Boolean,
    onSelect: () -> Unit,
    accentColor: Color,
    textColor: Color
) {
    Surface(
        modifier = Modifier.clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accentColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
        border = if (selected) BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
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
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) accentColor else textColor
                )
            )
        }
    }
}

/**
 * Results section with MainScreen styling
 */
@Composable
fun ResultsSection(
    loading: Boolean,
    error: String?,
    papers: List<Paper>,
    selectedDepartment: Department?,
    selectedSemester: Int?,
    selectedSubject: String?,
    searchQuery: String,
    onPaperDownload: (Paper) -> Unit,
    onClearError: () -> Unit,
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
                LoadingState(primaryColor = primaryColor, textColor = textColor)
            }
            error != null -> {
                ErrorState(
                    error = error,
                    onClearError = onClearError,
                    textColor = textColor
                )
            }
            papers.isEmpty() && (selectedDepartment != null || selectedSemester != null || selectedSubject != null || searchQuery.isNotEmpty()) -> {
                EmptyState(primaryColor = primaryColor, textColor = textColor)
            }
            selectedDepartment == null && selectedSemester == null && selectedSubject == null && searchQuery.isEmpty() -> {
                WelcomeState(primaryColor = primaryColor, textColor = textColor)
            }
            else -> {
                PapersList(
                    papers = papers,
                    onDownload = onPaperDownload,
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
fun LoadingState(primaryColor: Color, textColor: Color) {
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
                text = "Loading Papers...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = "Fetching the latest exam papers",
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
fun ErrorState(
    error: String,
    onClearError: () -> Unit,
    textColor: Color
) {
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
            horizontalAlignment =Alignment.CenterHorizontally
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
            Button(
                onClick = onClearError,
                modifier = Modifier.padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Try Again")
            }
        }
    }
}

/**
 * Empty state with MainScreen styling
 */
@Composable
fun EmptyState(primaryColor: Color, textColor: Color) {
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
                Icons.Rounded.SearchOff,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Papers Found",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = "Try adjusting your filters or search terms",
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
fun WelcomeState(primaryColor: Color, textColor: Color) {
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
                Icons.Rounded.MenuBook,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ready to Excel? 🎯",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = "Use the filters above to find past papers for your studies. Start by selecting your department!",
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
 * Papers list with MainScreen styling
 */
@Composable
fun PapersList(
    papers: List<Paper>,
    onDownload: (Paper) -> Unit,
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
                    Icons.Rounded.LibraryBooks,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Papers Found",
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
                    text = "${papers.size}",
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

    // Papers list
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(400.dp) // Fixed height to prevent overflow
    ) {
        items(papers) { paper ->
            PaperCard(
                paper = paper,
                onDownload = { onDownload(paper) },
                accentColor = accentColor,
                textColor = textColor
            )
        }
    }
}

/**
 * Individual paper card with MainScreen styling
 */
@Composable
fun PaperCard(
    paper: Paper,
    onDownload: () -> Unit,
    accentColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDownload() },
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
                        text = paper.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = paper.subject,
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
                        text = "Semester ${paper.semester}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = paper.year,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PastPapersScreenPreview() {
    NumlExamBuddyTheme {
        PastPapersScreen()
    }
}