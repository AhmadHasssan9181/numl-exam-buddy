package com.noobdev.numlexambuddy.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
    currentRoute: String = "",
    onNavigateToMain: () -> Unit = {},
    onNavigateToPastPapers: () -> Unit = {},
    onNavigateToLectures: () -> Unit = {},
    onNavigateToStudyMaterial: () -> Unit = {}
) {
    val primaryColor = Color(0xFF1E88E5) // Vibrant blue

    NavigationBar(
        containerColor = Color.White,
        contentColor = primaryColor,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("Home", Icons.Rounded.Home, "main_screen") to onNavigateToMain,
            Triple("Papers", Icons.Rounded.Assignment, "past_papers") to onNavigateToPastPapers,
            Triple("Lectures", Icons.Rounded.PlayCircleFilled, "lectures") to onNavigateToLectures,
            Triple("Material", Icons.AutoMirrored.Outlined.MenuBook, "study_material") to onNavigateToStudyMaterial
        )

        items.forEach { (item, onClick) ->
            val (title, icon, route) = item
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title
                    )
                },
                label = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = currentRoute == route,
                onClick = onClick,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryColor,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = primaryColor,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = primaryColor.copy(alpha = 0.1f)
                )
            )
        }
    }
}
