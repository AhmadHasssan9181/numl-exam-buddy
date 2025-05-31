package com.noobdev.numlexambuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noobdev.numlexambuddy.Screens.MainScreen
import com.noobdev.numlexambuddy.Screens.PastPapersScreen
import com.noobdev.numlexambuddy.Screens.LecturesScreen
import com.noobdev.numlexambuddy.Screens.StudyMaterialScreen
import com.noobdev.numlexambuddy.Screens.ProjectsScreen
import com.noobdev.numlexambuddy.navigation.NavRoutes
import com.noobdev.numlexambuddy.ui.theme.NumlExamBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NumlExamBuddyTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        AppNavHost(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAIN_SCREEN,
        modifier = modifier
    ) {        composable(NavRoutes.MAIN_SCREEN) {
            MainScreen(
                onNavigateToPastPapers = {
                    navController.navigate(NavRoutes.PAST_PAPERS)
                },
                onNavigateToLectures = {
                    navController.navigate(NavRoutes.LECTURES)
                },
                onNavigateToStudyMaterial = {
                    navController.navigate(NavRoutes.STUDY_MATERIAL)
                },                onNavigateToProjects = {
                    navController.navigate(NavRoutes.PROJECTS)
                },
                onNavigateToDocuments = {
                    navController.navigate(NavRoutes.STUDY_MATERIAL) // For now, point to Study Material
                }
            )
        }
        
        composable(NavRoutes.PAST_PAPERS) {
            // Implement back button handling for system navigation 
            BackHandler {
                navController.popBackStack()
            }
            
            PastPapersScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavRoutes.LECTURES) {
            BackHandler {
                navController.popBackStack()
            }

            LecturesScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavRoutes.STUDY_MATERIAL) {
            BackHandler {
                navController.popBackStack()
            }
            
            StudyMaterialScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )        }
        
        composable(NavRoutes.PROJECTS) {
            BackHandler {
                navController.popBackStack()
            }
            
            ProjectsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}