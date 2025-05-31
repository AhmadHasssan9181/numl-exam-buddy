package com.noobdev.numlexambuddy.Screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.MenuBook
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noobdev.numlexambuddy.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * Enhanced feature item with engaging properties
 */
data class StudentFeatureItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val itemCount: String,
    val trending: Boolean = false
)

/**
 * Dynamic MainScreen designed for students with rich animations and gradients
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToPastPapers: () -> Unit = {},
    onNavigateToLectures: () -> Unit = {},
    onNavigateToStudyMaterial: () -> Unit = {},
    onNavigateToProjects: () -> Unit = {},
    onNavigateToDocuments: () -> Unit = {}
) {
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var featuresVisible by remember { mutableStateOf(false) }
    var visibleItemCount by remember { mutableStateOf(0) }

    // Haptic feedback
    val haptic = LocalHapticFeedback.current

    // Floating animation for particles
    val infiniteTransition = rememberInfiniteTransition(label = "particle_animation")
    val particleAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_rotation"
    )

    // Scroll state for parallax
    val scrollState = rememberScrollState()

    // Student-focused features with vibrant gradients
    val features = remember {
        listOf(
            StudentFeatureItem(
                title = "Past Papers",
                subtitle = "Ace your exams with previous papers",
                icon = Icons.Rounded.Assignment,
                gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
                itemCount = "200+",
                trending = true
            ),
            StudentFeatureItem(
                title = "Video Lectures",
                subtitle = "Learn from expert professors",
                icon = Icons.Rounded.PlayCircleFilled,
                gradientColors = listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
                itemCount = "150+"
            ),
            StudentFeatureItem(
                title = "Study Notes",
                subtitle = "Comprehensive study materials",
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                gradientColors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe)),
                itemCount = "300+"
            ),
            StudentFeatureItem(
                title = "Projects Hub",
                subtitle = "Explore amazing student projects",
                icon = Icons.Rounded.Science,
                gradientColors = listOf(Color(0xFF43e97b), Color(0xFF38f9d7)),
                itemCount = "100+",
                trending = true
            )
        )
    }

    // Staggered animations with improved timing
    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
        delay(400)
        featuresVisible = true

        // Staggered feature cards reveal
        for (i in 1..features.size) {
            delay(120)
            visibleItemCount = i
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated background with floating particles
        AnimatedBackground(
            particleAnimation = particleAnimation,
            scrollOffset = scrollState.value
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Dynamic header with parallax
            AnimatedVisibility(
                visible = headerVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(tween(600))
            ) {
                DynamicHeader(scrollOffset = scrollState.value)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Features grid with staggered animations
            AnimatedVisibility(
                visible = featuresVisible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { it / 4 }
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    // Section title with gradient text and improved spacing
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        GradientText(
                            text = "🎓 Academic Resources",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            gradientColors = listOf(
                                Color(0xFF667eea),
                                Color(0xFFf093fb)
                            )
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.height(480.dp),
                        userScrollEnabled = false // Disable grid scrolling since parent scrolls
                    ) {
                        items(features.take(visibleItemCount)) { feature ->
                            StudentFeatureCard(
                                feature = feature,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    when(feature.title) {
                                        "Past Papers" -> onNavigateToPastPapers()
                                        "Video Lectures" -> onNavigateToLectures()
                                        "Study Notes" -> onNavigateToStudyMaterial()
                                        "Projects Hub" -> onNavigateToProjects()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp)) // Extra space for FAB
        }

        // Floating Action Button with improved animation
        AnimatedVisibility(
            visible = featuresVisible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(tween(400)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            PulsatingFAB(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    // TODO: Add help functionality
                }
            )
        }
    }
}

/**
 * Optimized animated background with floating particles
 */
@Composable
fun AnimatedBackground(
    particleAnimation: Float,
    scrollOffset: Int
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1a1a2e),
                    Color(0xFF16213e),
                    Color(0xFF0f3460)
                )
            )
        )

        // Floating particles with optimized count
        repeat(12) { index ->
            val angle = particleAnimation + (index * 0.5f)
            val radius = 40.dp.toPx() + (index * 12)
            val centerX = size.width * 0.5f + cos(angle) * radius
            val centerY = size.height * 0.3f + sin(angle * 0.8f) * radius - scrollOffset * 0.08f

            drawCircle(
                color = Color.White.copy(alpha = 0.08f - index * 0.004f),
                radius = (4 - index * 0.25f).dp.toPx().coerceAtLeast(1.dp.toPx()),
                center = Offset(centerX, centerY)
            )
        }

        // Gradient orbs with better positioning
        repeat(3) { index ->
            val orbX = size.width * (0.15f + index * 0.35f)
            val orbY = size.height * (0.12f + index * 0.08f) - scrollOffset * 0.04f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    radius = 120.dp.toPx()
                ),
                radius = 120.dp.toPx(),
                center = Offset(orbX, orbY)
            )
        }
    }
}

/**
 * Optimized dynamic header with smoother parallax
 */
@Composable
fun DynamicHeader(scrollOffset: Int) {
    val headerHeight = 180.dp
    val parallaxOffset = scrollOffset * 0.2f

    val logoRotation by rememberInfiniteTransition(label = "logo_rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_spin"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .offset(y = (-parallaxOffset * 0.4f).dp)
    ) {
        // Header gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF667eea).copy(alpha = 0.85f),
                            Color(0xFF764ba2).copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated logo with glow effect
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .rotate(logoRotation * 0.08f) // Slower rotation
            ) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF667eea).copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Logo
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = "NUML Exam Buddy",
                            tint = Color(0xFF667eea),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated title with improved shadow
            Text(
                text = "NUML Exam Buddy",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(1f, 2f),
                        blurRadius = 6f
                    )
                ),
                color = Color.White
            )

            // Improved subtitle animation
            AnimatedTypewriterText(
                text = "Your ultimate academic companion 🚀",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Optimized typewriter animation
 */
@Composable
fun AnimatedTypewriterText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(text) {
        currentIndex = 0
        displayedText = ""
        while (currentIndex < text.length) {
            delay(40) // Slightly faster typing
            displayedText = text.substring(0, currentIndex + 1)
            currentIndex++
        }
    }

    Text(
        text = displayedText,
        style = style,
        color = color,
        textAlign = TextAlign.Center
    )
}

/**
 * Enhanced feature card with better performance
 */
@Composable
fun StudentFeatureCard(
    feature: StudentFeatureItem,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 6f,
        animationSpec = tween(150),
        label = "card_elevation"
    )

    // Optimized particle animation
    val particleOffset by rememberInfiniteTransition(label = "card_particles").animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_movement"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = feature.gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
            // Optimized floating particles
            Canvas(modifier = Modifier.fillMaxSize()) {
                repeat(4) { index ->
                    val angle = particleOffset + (index * 1.5f)
                    val radius = 15.dp.toPx() + (index * 6)
                    val centerX = size.width * 0.75f + cos(angle) * radius * 0.2f
                    val centerY = size.height * 0.25f + sin(angle * 1.2f) * radius * 0.2f

                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f - index * 0.04f),
                        radius = (2.5f - index * 0.4f).dp.toPx().coerceAtLeast(0.5.dp.toPx()),
                        center = Offset(centerX, centerY)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Icon with optimized glow
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.25f),
                                        CircleShape
                                    )
                                    .blur(3.dp)
                            )

                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.92f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = feature.icon,
                                        contentDescription = feature.title,
                                        tint = feature.gradientColors[0],
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        // Trending badge
                        if (feature.trending) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "🔥",
                                    modifier = Modifier.padding(6.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    Text(
                        text = feature.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Bottom section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = feature.itemCount,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Resources",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Static arrow (removed animation for better performance)
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = "Open",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // Reset pressed state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(80)
            isPressed = false
        }
    }
}

/**
 * Gradient text component
 */
@Composable
fun GradientText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    gradientColors: List<Color>
) {
    Text(
        text = text,
        style = style.copy(
            brush = Brush.horizontalGradient(gradientColors)
        )
    )
}

/**
 * Enhanced FAB with click handler
 */
@Composable
fun PulsatingFAB(
    onClick: () -> Unit
) {
    val scale by rememberInfiniteTransition(label = "fab_pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .scale(scale),
        containerColor = Color.Transparent,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF667eea),
                            Color(0xFFf093fb)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Help,
                contentDescription = "Help",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun StudentMainScreenPreview() {
    NumlExamBuddyTheme {
        MainScreen()
    }
}