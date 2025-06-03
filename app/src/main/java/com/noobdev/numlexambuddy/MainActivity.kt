package com.noobdev.numlexambuddy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noobdev.numlexambuddy.Screens.AddDocumentScreen
import com.noobdev.numlexambuddy.Screens.DocumentDetailScreen
import com.noobdev.numlexambuddy.Screens.DocumentsScreen
import com.noobdev.numlexambuddy.Screens.LecturesScreen
import com.noobdev.numlexambuddy.Screens.MainScreen
import com.noobdev.numlexambuddy.Screens.PastPapersScreen
import com.noobdev.numlexambuddy.Screens.ProjectsScreen
import com.noobdev.numlexambuddy.Screens.StudyMaterialScreen
import com.noobdev.numlexambuddy.navigation.NavRoutes
import com.noobdev.numlexambuddy.ui.theme.NumlExamBuddyTheme
import com.noobdev.numlexambuddy.viewmodel.DocumentViewModel
import com.noobdev.numlexambuddy.viewmodel.DocumentViewModelFactory

class MainActivity : ComponentActivity() {
    
    // Document ViewModel
    private lateinit var documentViewModel: DocumentViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // Initialize the document ViewModel
        val app = application as ExamBuddyApplication
        val documentViewModelFactory = DocumentViewModelFactory(
            app.documentRepository,
            app.contentManager
        )
        documentViewModel = ViewModelProvider(this, documentViewModelFactory)
            .get(DocumentViewModel::class.java)
        
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
                            modifier = Modifier.padding(innerPadding),
                            documentViewModel = documentViewModel
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
    modifier: Modifier = Modifier,
    documentViewModel: DocumentViewModel
) {    
    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAIN_SCREEN,
        modifier = modifier
    ) {        
        composable(NavRoutes.MAIN_SCREEN) {
            MainScreen(
                onNavigateToPastPapers = {
                    navController.navigate(NavRoutes.PAST_PAPERS)
                },
                onNavigateToLectures = {
                    navController.navigate(NavRoutes.LECTURES)
                },
                onNavigateToStudyMaterial = {
                    navController.navigate(NavRoutes.STUDY_MATERIAL)
                },                
                onNavigateToProjects = {
                    navController.navigate(NavRoutes.PROJECTS)
                },
                onNavigateToDocuments = {
                    navController.navigate(NavRoutes.DOCUMENTS)
                }
            )
        }
        
        // Documents Screen
        composable(NavRoutes.DOCUMENTS) {
            BackHandler {
                navController.popBackStack()
            }
            
            DocumentsScreen(
                viewModel = documentViewModel,
                onNavigateBack = { navController.popBackStack() },
                onDocumentClick = { document -> 
                    navController.navigate("${NavRoutes.DOCUMENT_DETAIL}/${document.id}")
                },
                onAddDocumentClick = {
                    navController.navigate(NavRoutes.DOCUMENT_ADD)
                }
            )
        }
          // Document Detail Screen
        composable(
            route = "${NavRoutes.DOCUMENT_DETAIL}/{documentId}",
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: ""
            
            BackHandler {
                navController.popBackStack()
            }
            
            DocumentDetailScreen(
                viewModel = documentViewModel,
                documentId = documentId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { document ->
                    navController.navigate("${NavRoutes.DOCUMENT_CHAT}/${document.id}")
                },
                onNavigateToEdit = { document ->
                    // Navigate to edit document when implemented
                }
            )
        }
        
        // Add Document Screen
        composable(NavRoutes.DOCUMENT_ADD) {
            BackHandler {
                navController.popBackStack()
            }
            
            AddDocumentScreen(
                viewModel = documentViewModel,
                onNavigateBack = { navController.popBackStack() },
                onDocumentAdded = { documentId ->
                    // Navigate to document detail screen after document is added
                    navController.navigate("${NavRoutes.DOCUMENT_DETAIL}/${documentId}") {
                        // Remove the add screen from the back stack
                        popUpTo(NavRoutes.DOCUMENT_ADD) { inclusive = true }
                    }
                }
            )
        }
        
        // Add existing routes
        composable(NavRoutes.PAST_PAPERS) {
            // Implement back button handling for system navigation 
            BackHandler {
                navController.popBackStack()
            }
            
            PastPapersScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    navController.navigate(NavRoutes.MAIN_SCREEN) {
                        popUpTo(NavRoutes.MAIN_SCREEN) { inclusive = false }
                    }
                },
                onNavigateToLectures = {
                    navController.navigate(NavRoutes.LECTURES)
                },
                onNavigateToStudyMaterial = {
                    navController.navigate(NavRoutes.STUDY_MATERIAL)
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
                },
                onNavigateToMain = {
                    navController.navigate(NavRoutes.MAIN_SCREEN) {
                        popUpTo(NavRoutes.MAIN_SCREEN) { inclusive = false }
                    }
                },
                onNavigateToPastPapers = {
                    navController.navigate(NavRoutes.PAST_PAPERS)
                },
                onNavigateToStudyMaterial = {
                    navController.navigate(NavRoutes.STUDY_MATERIAL)
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
                },
                onNavigateToMain = {
                    navController.navigate(NavRoutes.MAIN_SCREEN) {
                        popUpTo(NavRoutes.MAIN_SCREEN) { inclusive = false }
                    }
                },
                onNavigateToPastPapers = {
                    navController.navigate(NavRoutes.PAST_PAPERS)
                },
                onNavigateToLectures = {
                    navController.navigate(NavRoutes.LECTURES)
                }
            )
        }
          composable(NavRoutes.PROJECTS) {
            BackHandler {
                navController.popBackStack()
            }
            
            ProjectsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    navController.navigate(NavRoutes.MAIN_SCREEN) {
                        popUpTo(NavRoutes.MAIN_SCREEN) { inclusive = false }
                    }
                },
                onNavigateToPastPapers = {
                    navController.navigate(NavRoutes.PAST_PAPERS)
                },
                onNavigateToLectures = {
                    navController.navigate(NavRoutes.LECTURES)
                },
                onNavigateToStudyMaterial = {
                    navController.navigate(NavRoutes.STUDY_MATERIAL)
                }
            )
        }
    }
}