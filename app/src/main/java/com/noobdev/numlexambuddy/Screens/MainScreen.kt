package com.noobdev.numlexambuddy.Screens

import android.R.attr.shape
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noobdev.numlexambuddy.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Data class representing a feature item in the academic dashboard
 */
data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val backgroundColor: List<Color>,
    val description: String = ""
)

/**
 * Main screen of the NUML Exam Buddy application
 * Features a sophisticated UI with subtle animations
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    onNavigateToPastPapers: () -> Unit = {},
    onNavigateToTestDrive: () -> Unit = {}
) {
    // Coroutine scope for animations
    val scope = rememberCoroutineScope()

    // Scroll state for parallax effect
    val scrollState = rememberScrollState()

    // Animation visibility states with staggered timing
    var headerVisible by remember { mutableStateOf(false) }
    var navBarVisible by remember { mutableStateOf(false) }
    var searchVisible by remember { mutableStateOf(false) }
    var sectionTitleVisible by remember { mutableStateOf(false) }
    var featuresVisible by remember { mutableStateOf(false) }

    // Animation for staggered item entry
    var visibleItems by remember { mutableStateOf(0) }

    // Start animations when the screen loads with professional timing
    LaunchedEffect(key1 = Unit) {
        delay(100)
        headerVisible = true
        delay(200)
        navBarVisible = true
        delay(100)
        searchVisible = true
        delay(150)
        sectionTitleVisible = true
        delay(100)
        featuresVisible = true

        // Staggered animation for grid items
        for (i in 1..8) {
            delay(80)
            visibleItems = i
        }
    }

    // Feature items definition with more subtle gradients and professional icons
    val featureItems = remember {
        listOf(
            FeatureItem(
                "Past Papers",
                Icons.Outlined.Description,
                GradientMonochrome,
                "Access previous exam papers"
            ),
            FeatureItem(
                "Test Drive API",
                Icons.Outlined.Dns,
                GradientPurple,
                "Test Google Drive connection"
            ),
            FeatureItem(
                "Lectures",
                Icons.Outlined.VideoLibrary,
                GradientSlate,
                "Watch recorded lectures"
            ),            
            FeatureItem(
                "Study Material",
                Icons.AutoMirrored.Outlined.MenuBook,
                GradientDeepBlue,
                "Access notes and study guides"
            ),
            FeatureItem(
                "Upload Papers",
                Icons.Outlined.Upload,
                GradientDarkTeal,
                "Share papers with others"
            ),
            FeatureItem(
                "Prof Reviews",
                Icons.Outlined.RateReview,
                GradientCharcoal,
                "Read and write professor reviews"
            ),            FeatureItem(
                "Add Documents",
                Icons.AutoMirrored.Outlined.NoteAdd,
                GradientGray,
                "Contribute study documents"
            ),
            FeatureItem(
                "Feedback",
                Icons.Outlined.Feedback,
                GradientDusk,
                "Share your suggestions"
            ),
            FeatureItem(
                "AI Help",
                Icons.Outlined.Psychology,
                GradientNavy,
                "Get AI assistance with your studies"
            )
        )
    }

    // Main container with theme-aware background
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrollable column for the entire screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 24.dp)
            ) {
                // Parallax header effect based on scroll
                val headerHeight = 160.dp
                val parallaxFactor = with(LocalDensity.current) {
                    (scrollState.value * 0.15f).toDp()
                }

                // Animated header section with parallax effect
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(700)) + slideInVertically(
                        initialOffsetY = { -150 },
                        animationSpec = tween(700, easing = EaseOutQuint)
                    )
                ) {
                    Box(
                        modifier = Modifier.height(headerHeight - parallaxFactor)
                    ) {
                        HeaderSection(scrollOffset = scrollState.value)
                    }
                }

                // Animated navigation bar
                AnimatedVisibility(
                    visible = navBarVisible,
                    enter = fadeIn(tween(500)) + expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(500, easing = EaseOutCubic)
                    )
                ) {
                    NavigationTabs()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Main content padding for proper alignment
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Search bar with subtle animation
                    AnimatedVisibility(
                        visible = searchVisible,
                        enter = fadeIn(tween(800)) + expandHorizontally(
                            animationSpec = tween(600, easing = EaseOutSine)
                        )
                    ) {
                        SearchBar()
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section title with animation
                    AnimatedVisibility(
                        visible = sectionTitleVisible,
                        enter = fadeIn(tween(600)) + slideInHorizontally(
                            initialOffsetX = { -40 },
                            animationSpec = tween(500, easing = EaseOutCubic)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 20.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Academic Resources",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Animated features grid with staggered reveal
                    AnimatedVisibility(
                        visible = featuresVisible,
                        enter = fadeIn(tween(800))
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp) // Fixed height for proper preview
                        ) {
                            items(featureItems.take(visibleItems)) { item ->
                                FeatureCard(
                                    feature = item,
                                    onClick = {
                                        when(item.title) {
                                            "Past Papers" -> onNavigateToPastPapers()
                                            "Test Drive API" -> onNavigateToTestDrive()
                                            // Add other navigation routes as they are implemented
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // FAB with subtle entrance - Moved to Box scope to avoid alignment issues
            AnimatedVisibility(
                visible = featuresVisible,
                enter = fadeIn(tween(1000)) + scaleIn(
                    initialScale = 0.6f,
                    animationSpec = tween(800, easing = EaseOutBack)
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)  // Now in Box scope, this works correctly
            ) {
                SmallFloatingActionButton(
                    onClick = { /* TODO: Implement help action */ },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Help,
                        contentDescription = "Help"
                    )
                }
            }
        }
    }
}

/**
 * Header section with parallax scrolling effect
 */
@Composable
fun HeaderSection(scrollOffset: Int = 0) {
    // Calculate parallax offsets
    val parallaxOffset = scrollOffset * 0.2f

    // Logo alpha based on scroll
    val logoAlpha = 1f - (scrollOffset / 1000f).coerceIn(0f, 0.3f)

    // Create subtle breathing animation for the logo
    val infiniteTransition = rememberInfiniteTransition()
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Apply combined scale
    val combinedScale = breathingScale * (1f - (scrollOffset / 2000f)).coerceIn(0.95f, 1f)

    // Header card with elevation that reduces on scroll
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 - (scrollOffset / 100f).coerceIn(0f, 3f)).dp
        )
    ) {
        // Background gradient with parallax effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = -(parallaxOffset).dp)  // Fixed syntax
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryLight,
                            PrimaryVariant
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Content row with logo and text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .offset(y = -(parallaxOffset / 2f).dp)  // Fixed syntax
            ) {
                // App logo with animated effect
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .scale(combinedScale)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    // Logo icon with breathing animation
                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = "NUML Exam Buddy Logo",
                        tint = PrimaryLight,
                        modifier = Modifier
                            .size(40.dp)
                            .alpha(logoAlpha)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // App title and tagline with fade effect
                Column(
                    modifier = Modifier.alpha(logoAlpha)
                ) {
                    Text(
                        text = "NUML Exam Buddy",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal
                    )

                    // Animated typing effect for the tagline
                    AnimatedTypingText(
                        text = "Your academic companion",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Animated typing text effect
 */
@Composable
fun AnimatedTypingText(
    text: String,
    color: Color,
    style: TextStyle
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = text) {
        while (currentIndex < text.length) {
            delay(50)
            displayedText = text.substring(0, currentIndex + 1)
            currentIndex++
        }
    }

    Text(
        text = displayedText,
        color = color,
        style = style
    )
}

/**
 * Navigation tabs with indicator animation
 */
@Composable
fun NavigationTabs() {
    val options = listOf("Dashboard", "Calendar", "Favorites", "Profile")
    var selectedOption by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(48.dp)
            .shadow(1.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = selectedOption == index

                // Animate color and indicator for selected tab
                val itemColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    animationSpec = tween(300)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { selectedOption = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = itemColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if(isSelected) FontWeight.Medium else FontWeight.Normal
                    )

                    // Animated indicator
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 2.dp)
                                .width(24.dp)
                                .height(2.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Search bar component with elegant styling - Fixed OutlinedTextField implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    var isFocused by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .onFocusChanged { isFocused = it.isFocused },
        placeholder = {
            Text(
                "Search resources...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "Search",
                tint = if (isFocused)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * Enhanced feature card with elegant hover and press animations
 */
@Composable
fun FeatureCard(
    feature: FeatureItem,
    onClick: () -> Unit = {}
) {
    // State for animations
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Scale animation when pressed - very subtle
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.97f
            isHovered -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Elevation animation for hover effect
    val elevation by animateFloatAsState(
        targetValue = when {
            isHovered -> 4f
            else -> 1f
        },
        animationSpec = tween(200)
    )    // Subtle border animation on hover
    val borderWidth by animateFloatAsState(
        targetValue = if (isHovered) 0.5f else 0f,
        animationSpec = tween(200)
    )

    // Card component with animations and styling
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(6.dp),
        border = if (borderWidth > 0)
            BorderStroke(borderWidth.dp, Color.White.copy(alpha = 0.3f))
        else null,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(feature.backgroundColor)
                )
        ) {
            // Content column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon with subtle rotation animation
                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated halo effect on hover
                    if (isHovered) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }

                    // Icon
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = feature.title,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feature title
                Text(
                    text = feature.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                // Feature description
                if (feature.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = feature.description,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Reveal "View" button on hover
                AnimatedVisibility(
                    visible = isHovered,
                    enter = fadeIn(tween(200)) + expandVertically(
                        expandFrom = Alignment.Bottom
                    ),
                    exit = fadeOut(tween(200))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Reset pressed state after animation completes
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

/**
 * Preview function for the main screen
 */
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun MainScreenPreview() {
    NumlExamBuddyTheme {
        MainScreen(
            onNavigateToPastPapers = {}
        )
    }
}