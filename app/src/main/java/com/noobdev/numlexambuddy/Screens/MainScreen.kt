package com.noobdev.numlexambuddy.Screens

import android.R.attr.resource
import android.R.string.no
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noobdev.numlexambuddy.ui.theme.*
import com.noobdev.numlexambuddy.components.BottomNavBar

/**
 * Enhanced feature item with engaging properties
 * Theme Integration: Uses MaterialTheme colors for consistency
 */
data class StudentFeatureItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val itemCount: String,
    val rating: Float = 4.5f,
)

/**
 * Modernized MainScreen designed for students with clean UI
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
    // Haptic feedback
    val haptic = LocalHapticFeedback.current    // Use theme colors instead of hardcoded colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.customColors.accentAmber
    val backgroundGray = MaterialTheme.colorScheme.background
    val cardBackgroundColor = MaterialTheme.colorScheme.surface
    val textPrimaryColor = MaterialTheme.colorScheme.onBackground    // Recommended study resources
    // Recommended study resources
    val surfaceColor = MaterialTheme.colorScheme.surface
    val recommendedResources = remember {
        listOf(
            StudentFeatureItem(
                title = "Past Papers",
                subtitle = "Exam preparation",
                icon = Icons.Rounded.Assignment,
                backgroundColor = surfaceColor,
                itemCount = "200+",
                rating = 4.7f
            ),
            StudentFeatureItem(
                title = "Lectures",
                subtitle = "Core concepts",
                icon = Icons.Rounded.PlayCircleFilled,
                backgroundColor = surfaceColor,
                itemCount = "150+",
                rating = 4.8f
            ),
            StudentFeatureItem(
                title = "Study Notes",
                subtitle = "Comprehensive materials",
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                backgroundColor = surfaceColor,
                itemCount = "300+",
                rating = 4.4f
            ),
            StudentFeatureItem(
                title = "Projects Hub",
                subtitle = "Student projects",
                icon = Icons.Rounded.Science,
                backgroundColor = surfaceColor,
                itemCount = "100+",
                rating = 4.2f
            )
        )
    }
    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "main_screen",
                onNavigateToMain = { /* Already on main */ },
                onNavigateToPastPapers = onNavigateToPastPapers,
                onNavigateToLectures = onNavigateToLectures,
                onNavigateToStudyMaterial = onNavigateToStudyMaterial
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundGray)
                .verticalScroll(rememberScrollState())
        ) {
            // Header section
            HeaderSection(primaryColor)

            Spacer(modifier = Modifier.height(8.dp))

            // Recommended Section
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                SectionHeader(
                    title = "Study Material",
                    onSeeAllClick = {},
                    textColor = textPrimaryColor
                )

                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp) // Adjust height as needed
                ) {
                    items(recommendedResources.size) { index ->
                        ResourceCard(
                            resource = recommendedResources[index],                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                when(recommendedResources[index].title) {
                                    "Past Papers" -> onNavigateToPastPapers()
                                    "Lectures" -> onNavigateToLectures()
                                    "Study Notes" -> onNavigateToStudyMaterial()
                                    "Projects Hub" -> onNavigateToProjects()
                                }
                            },
                            accentColor = accentColor,
                            textColor = textPrimaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(primaryColor: Color) {
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
            // Top bar with profile and notification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile icon - FIXED: Using Image instead of Icon for PNG
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Image(
                        painter = painterResource(id = com.noobdev.numlexambuddy.R.drawable.numl_logo),
                        contentDescription = "NUML Logo",
                        modifier = Modifier
                            .padding(1.dp)
                            .fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Welcome text
            Text(
                text = "Delicious knowledge ready to be delivered for you 📚",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                )
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}

@Composable
fun ResourceCard(
    resource: StudentFeatureItem,
    onClick: () -> Unit,
    accentColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(170.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Resource icon as "image"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = resource.icon,
                    contentDescription = resource.title,
                    tint = accentColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SmallResourceCard(
    resource: StudentFeatureItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {            // Resource icon as "image"
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {                Icon(
                    imageVector = resource.icon,
                    contentDescription = resource.title,
                    tint = MaterialTheme.customColors.accentAmber,
                    modifier = Modifier.size(42.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = resource.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }                // Rating row
                Row(verticalAlignment = Alignment.CenterVertically) {                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.customColors.accentAmber,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = "${resource.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = " · ${resource.itemCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun StudentMainScreenPreviews() {
    NumlExamBuddyTheme {
        MainScreen()
    }
}