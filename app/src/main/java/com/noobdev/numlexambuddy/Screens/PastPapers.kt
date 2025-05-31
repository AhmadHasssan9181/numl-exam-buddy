package com.noobdev.numlexambuddy.Screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlin.math.*

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
 * Redesigned PastPapersScreen with modern student-friendly UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastPapersScreen(
    viewModel: PastPapersViewModel = viewModel(
        factory = PastPapersViewModelFactory(LocalContext.current)
    ),
    onBack: () -> Unit = {}
) {
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var filtersVisible by remember { mutableStateOf(false) }
    var resultsVisible by remember { mutableStateOf(false) }
    var searchExpanded by remember { mutableStateOf(false) }

    // Haptic feedback
    val haptic = LocalHapticFeedback.current

    // Background animation
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    val backgroundAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "background_rotation"
    )

    // Sample departments
    val departments = remember {
        listOf(
            Department("Computer Science", "BSCS"),
            Department("Artificial Intelligence", "BSAI"),
            Department("Information Technology", "BSIT"),
            Department("Software Engineering", "BSSE"),
            Department("Associate Degree in Computing", "ADC")
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

    // Filter papers
    val filteredPapers = remember(papers, searchQuery) {
        papers.filter { paper ->
            searchQuery.isEmpty() ||
                    paper.title.contains(searchQuery, ignoreCase = true) ||
                    paper.subject.contains(searchQuery, ignoreCase = true)
        }
    }

    // Entrance animations
    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
        delay(200)
        filtersVisible = true
        delay(300)
        resultsVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated background
        StudyBackground(
            animationProgress = backgroundAnimation,
            alpha = 0.03f
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Modern Header
            AnimatedVisibility(
                visible = headerVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) + fadeIn(tween(600))
            ) {
                ModernHeader(
                    onBack = onBack,
                    onSearch = { searchExpanded = !searchExpanded }
                )
            }

            // Search Bar
            AnimatedVisibility(
                visible = searchExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) + fadeIn()
            ) {
                SearchSection(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onClose = { searchExpanded = false }
                )
            }

            // Filters Section
            AnimatedVisibility(
                visible = filtersVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) + fadeIn(tween(800))
            ) {
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
                    }
                )
            }

            // Results Section
            AnimatedVisibility(
                visible = resultsVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 }
                ) + fadeIn(tween(1000))
            ) {
                ResultsSection(
                    loading = loading,
                    error = error,
                    papers = filteredPapers,
                    selectedDepartment = selectedDepartment,
                    selectedSemester = selectedSemester,
                    selectedSubject = selectedSubject,
                    onPaperDownload = { paper ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.downloadPaper(paper)
                    },
                    onClearError = { viewModel.clearError() }
                )
            }
        }
    }
}

/**
 * Animated background for papers screen
 */
@Composable
fun StudyBackground(
    animationProgress: Float,
    alpha: Float = 0.05f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1a1a2e).copy(alpha = 0.3f),
                    Color(0xFF16213e).copy(alpha = 0.2f),
                    Color.Transparent
                )
            )
        )

        // Floating academic elements
        repeat(8) { index ->
            val angle = animationProgress + (index * 0.8f)
            val radius = 60.dp.toPx() + (index * 15)
            val centerX = size.width * (0.2f + index * 0.1f) + cos(angle) * radius * 0.3f
            val centerY = size.height * (0.1f + index * 0.08f) + sin(angle * 0.6f) * radius * 0.2f

            // Academic icons as circles
            drawCircle(
                color = Color(0xFF667eea).copy(alpha = alpha),
                radius = (6 - index * 0.5f).dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        // Paper-like floating rectangles
        repeat(5) { index ->
            val angle = animationProgress * 0.7f + (index * 1.2f)
            val offsetX = size.width * (0.3f + index * 0.15f) + cos(angle) * 40.dp.toPx()
            val offsetY = size.height * (0.2f + index * 0.12f) + sin(angle * 0.8f) * 30.dp.toPx()

            drawRoundRect(
                color = Color.White.copy(alpha = alpha * 0.5f),
                topLeft = Offset(offsetX, offsetY),
                size = androidx.compose.ui.geometry.Size(
                    width = 20.dp.toPx(),
                    height = 25.dp.toPx()
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
            )
        }
    }
}

/**
 * Modern header with gradient and animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernHeader(
    onBack: () -> Unit,
    onSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.9f),
                        Color(0xFF764ba2).copy(alpha = 0.8f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 800f)
                )
            )
    ) {
        // Header content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button with glow
                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                            .blur(2.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onBack() },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Go back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "📄 Past Papers",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.2f),
                                offset = Offset(1f, 1f),
                                blurRadius = 4f
                            )
                        ),
                        color = Color.White
                    )

                    Text(
                        text = "Access previous exam papers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Search button
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onSearch() },
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Enhanced search section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "🔍 Search papers by title or subject...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color(0xFF667eea)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF667eea),
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF667eea).copy(alpha = 0.05f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onClose) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Enhanced filters section with better design
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
    onSubjectSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFF667eea).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.FilterList,
                        contentDescription = null,
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    "Filter Papers",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Department selection
            FilterCategory(
                title = "📚 Department",
                description = "Select your department"
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(departments) { department ->
                        ModernDepartmentChip(
                            department = department,
                            selected = department == selectedDepartment,
                            onSelect = { onDepartmentSelect(department) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Semester selection
            FilterCategory(
                title = "📅 Semester",
                description = "Choose semester"
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items((1..8).toList()) { semester ->
                        ModernSemesterChip(
                            semester = semester,
                            selected = semester == selectedSemester,
                            onSelect = { onSemesterSelect(semester) }
                        )
                    }
                }
            }

            // Subject selection (only show if subjects available)
            if (subjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                FilterCategory(
                    title = "📖 Subject",
                    description = "Pick a subject"
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(subjects) { subject ->
                            ModernSubjectChip(
                                subject = subject,
                                selected = subject == selectedSubject,
                                onSelect = { onSubjectSelect(subject) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filter category component
 */
@Composable
fun FilterCategory(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
        )

        content()
    }
}

/**
 * Modern department chip with gradient
 */
@Composable
fun ModernDepartmentChip(
    department: Department,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "department_scale"
    )

    Card(
        modifier = Modifier
            .scale(scale)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                Color(0xFF667eea).copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 6.dp else 2.dp
        ),
        border = if (selected) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = department.code,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (selected) Color(0xFF667eea) else MaterialTheme.colorScheme.primary
            )

            Text(
                text = department.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Modern semester chip
 */
@Composable
fun ModernSemesterChip(
    semester: Int,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "semester_scale"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .background(
                brush = if (selected) Brush.linearGradient(
                    colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                ) else Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = if (selected) 0.dp else 2.dp,
                color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$semester",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Modern subject chip
 */
@Composable
fun ModernSubjectChip(
    subject: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "subject_scale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected)
            Color(0xFF43e97b).copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surface,
        border = if (selected)
            BorderStroke(
            1.dp,
            Color(0xFF43e97b).copy(alpha = 0.5f)
        ) else
            BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = subject,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) Color(0xFF43e97b) else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Enhanced results section
 */
@Composable
fun ResultsSection(
    loading: Boolean,
    error: String?,
    papers: List<Paper>,
    selectedDepartment: Department?,
    selectedSemester: Int?,
    selectedSubject: String?,
    onPaperDownload: (Paper) -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Results header
        if (!loading && error == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF43e97b).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(0xFF43e97b).copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Description,
                                contentDescription = null,
                                tint = Color(0xFF43e97b),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            "Available Papers",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF43e97b).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${papers.size} results",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF43e97b),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Content
        when {
            loading -> {
                LoadingState()
            }
            error != null -> {
                ErrorState(
                    error = error,
                    onClearError = onClearError
                )
            }
            papers.isEmpty() && selectedDepartment != null && selectedSemester != null && selectedSubject != null -> {
                EmptyPapersState()
            }
            selectedDepartment == null || selectedSemester == null || selectedSubject == null -> {
                SelectFiltersState()
            }
            else -> {
                PapersList(
                    papers = papers,
                    onDownload = onPaperDownload
                )
            }
        }
    }
}

/**
 * Loading state with animated indicator
 */
@Composable
fun LoadingState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF667eea),
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading papers...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = "Please wait while we fetch the latest papers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Error state
 */
@Composable
fun ErrorState(
    error: String,
    onClearError: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
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
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Button(
                onClick = onClearError,
                modifier = Modifier.padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Try Again")
            }
        }
    }

    // Auto-clear error
    LaunchedEffect(error) {
        delay(5000)
        onClearError()
    }
}

/**
 * Empty papers state
 */
@Composable
fun EmptyPapersState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
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
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Papers Found",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = "We couldn't find any past papers for this combination. Try selecting different filters or check back later!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Select filters state
 */
@Composable
fun SelectFiltersState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF667eea).copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.Tune,
                contentDescription = null,
                tint = Color(0xFF667eea),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ready to Find Papers? 📚",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Select your department, semester, and subject above to discover available past papers for your studies.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Papers list
 */
@Composable
fun PapersList(
    papers: List<Paper>,
    onDownload: (Paper) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(papers) { paper ->
            ModernPaperCard(
                paper = paper,
                onDownload = { onDownload(paper) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Modern paper card with enhanced design
 */
@Composable
fun ModernPaperCard(
    paper: Paper,
    onDownload: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isExpanded = !isExpanded
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = paper.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = paper.subject,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFf093fb).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = paper.year,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFf093fb),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF667eea).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Semester ${paper.semester}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF667eea),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF43e97b).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = paper.department,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF43e97b),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Download button
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) + fadeIn()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = paper.downloadUrl.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (paper.downloadUrl.isNotEmpty()) Icons.Rounded.Download else Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (paper.downloadUrl.isNotEmpty()) "Download Paper" else "Not Available",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ModernPastPapersScreenPreview() {
    NumlExamBuddyTheme {
        PastPapersScreen(onBack = {})
    }
}