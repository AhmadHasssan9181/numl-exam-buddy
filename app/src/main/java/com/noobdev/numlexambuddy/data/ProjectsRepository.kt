package com.noobdev.numlexambuddy.data

import android.util.Log
import com.google.api.services.drive.Drive
import com.noobdev.numlexambuddy.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProjectsRepository(private val driveService: Drive) {
    
    private val TAG = "ProjectsRepository"
    private val rootFolderId = "1wnOHvbCCBYTOLvfL6UJjzQdenkQ4Bk8r" // Same as other working repositories
    private val folderCache = mutableMapOf<String, String>()
    
    // Helper method to get a folder ID by name (copied from working repositories)
    private suspend fun getFolderId(parentId: String, folderName: String): String? {
        val cacheKey = "$parentId:$folderName"
        folderCache[cacheKey]?.let { return it }

        val result = driveService.files().list()
            .setQ("'$parentId' in parents and name='$folderName' and mimeType='application/vnd.google-apps.folder' and trashed=false")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val folderId = result.files.firstOrNull()?.id
        // Cache the result
        if (folderId != null) {
            folderCache[cacheKey] = folderId
        }
        return folderId
    }    suspend fun getDepartments(): List<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to load departments from Google Drive using rootFolderId: $rootFolderId")
            
            val result = driveService.files().list()
                .setQ("'$rootFolderId' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
            
            val departments = result.files?.map { it.name }?.sorted() ?: emptyList()
            Log.d(TAG, "Successfully loaded ${departments.size} departments from Drive: $departments")
            return@withContext departments
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load departments from Drive: ${e.message}", e)
            // Return default departments if Drive access fails
            val defaultDepartments = listOf("BSCS", "BSAI", "BSIT", "BSSE", "ADC")
            Log.d(TAG, "Using default departments: $defaultDepartments")
            return@withContext defaultDepartments
        }
    }
    suspend fun getSemesters(department: String): List<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to load semesters for department: $department")
            val departmentFolderId = getFolderId(rootFolderId, department)
                ?: throw Exception("Department folder '$department' not found")
            
            Log.d(TAG, "Found department folder $department with ID: $departmentFolderId")
            
            val result = driveService.files().list()
                .setQ("'$departmentFolderId' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
            
            val semesters = result.files?.map { it.name }?.sorted() ?: emptyList()
            Log.d(TAG, "Successfully loaded ${semesters.size} semesters for $department: $semesters")
            return@withContext semesters
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load semesters for $department: ${e.message}", e)
            // Return default semesters if Drive access fails
            val defaultSemesters = (1..8).map { "SEMESTER-$it" }
            Log.d(TAG, "Using default semesters: $defaultSemesters")
            return@withContext defaultSemesters
        }
    }
    suspend fun getSubjects(department: String, semester: String): List<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to load subjects for $department - $semester")
            val departmentFolderId = getFolderId(rootFolderId, department)
                ?: throw Exception("Department folder '$department' not found")
            val semesterFolderId = getFolderId(departmentFolderId, semester)
                ?: throw Exception("Semester folder '$semester' not found in department '$department'")
            
            val result = driveService.files().list()
                .setQ("'$semesterFolderId' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
            
            val subjects = result.files?.map { it.name }?.sorted() ?: emptyList()
            Log.d(TAG, "Successfully loaded ${subjects.size} subjects for $department - $semester: $subjects")
            return@withContext subjects
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load subjects for $department - $semester: ${e.message}", e)
            // Return mock subjects if Drive access fails
            val mockSubjects = when (department to semester) {
                "BSCS" to "SEMESTER-1" -> listOf("Programming Fundamentals", "Mathematics I", "Physics", "English")
                "BSCS" to "SEMESTER-2" -> listOf("Object Oriented Programming", "Data Structures", "Mathematics II", "Statistics")
                "BSCS" to "SEMESTER-3" -> listOf("Database Systems", "Computer Networks", "Software Engineering", "Algorithms")
                "BSAI" to "SEMESTER-1" -> listOf("AI Fundamentals", "Mathematics I", "Programming", "Logic")
                "BSAI" to "SEMESTER-2" -> listOf("Machine Learning", "Data Mining", "Statistics", "Python Programming")
                "BSIT" to "SEMESTER-1" -> listOf("IT Fundamentals", "Computer Systems", "Programming", "Mathematics")
                "BSSE" to "SEMESTER-1" -> listOf("Software Engineering", "Programming", "Requirements Engineering", "Mathematics")
                "ADC" to "SEMESTER-1" -> listOf("Computer Basics", "Programming", "Mathematics", "English")
                else -> listOf("Sample Subject 1", "Sample Subject 2", "Sample Subject 3")
            }
            Log.d(TAG, "Using mock subjects for $department - $semester: $mockSubjects")
            return@withContext mockSubjects
        }
    }
    suspend fun getProjects(department: String, semester: String, subject: String):
            List<Project> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to load projects for $department - $semester - $subject")
            
            val departmentFolderId = getFolderId(rootFolderId, department)
                ?: throw Exception("Department '$department' not found")
            val semesterFolderId = getFolderId(departmentFolderId, semester)
                ?: throw Exception("Semester '$semester' not found in department '$department'")
            val subjectFolderId = getFolderId(semesterFolderId, subject)
                ?: throw Exception("Subject '$subject' not found in semester '$semester'")
            
            // Try to find the "projects" folder (based on drive structure)
            val projectsFolderId = try {
                getFolderId(subjectFolderId, "projects")
                    ?: getFolderId(subjectFolderId, "Projects")  // fallback
                    ?: getFolderId(subjectFolderId, "PROJECTS")  // fallback
                    ?: subjectFolderId  // If no specific projects folder, use subject folder
            } catch (e: Exception) {
                subjectFolderId  // Default to subject folder
            }
            
            Log.d(TAG, "Found projects folder for subject: $subject")
            
            // Get all files in the projects folder
            val result = driveService.files().list()
                .setQ("'$projectsFolderId' in parents and mimeType!='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name, size, webViewLink, mimeType)")
                .execute()
            
            val projects = result.files?.map { file ->
                Project(
                    id = file.id,
                    name = file.name,
                    size = formatFileSize(file.getSize()),
                    downloadUrl = file.webViewLink ?: "",
                    mimeType = file.mimeType ?: "",
                    subject = subject,
                    department = department,
                    semester = semester.removePrefix("SEMESTER-").toIntOrNull() ?: 1,
                    type = "Real Project"
                )
            } ?: emptyList()
            
            Log.d(TAG, "Successfully loaded ${projects.size} real projects for $department - $semester - $subject")
            return@withContext projects} catch (e: Exception) {
            Log.e(TAG, "Failed to load projects for $department - $semester - $subject: ${e.message}", e)
            // Return mock projects if Drive access fails
            listOf(
                Project(
                    id = "mock1",
                    name = "$subject - Sample Project 1",
                    size = "2.5 MB",
                    downloadUrl = "https://drive.google.com/file/d/mock1/view",
                    mimeType = "application/zip",
                    subject = subject,
                    department = department,
                    semester = semester.removePrefix("SEMESTER-").toIntOrNull() ?: 1,
                    type = "Sample Project"
                ),
                Project(
                    id = "mock2", 
                    name = "$subject - Sample Project 2",
                    size = "1.8 MB",
                    downloadUrl = "https://drive.google.com/file/d/mock2/view",
                    mimeType = "application/zip",
                    subject = subject,
                    department = department,
                    semester = semester.removePrefix("SEMESTER-").toIntOrNull() ?: 1,
                    type = "Sample Project"
                ),
                Project(
                    id = "mock3",
                    name = "$subject - Sample Project 3", 
                    size = "3.2 MB",
                    downloadUrl = "https://drive.google.com/file/d/mock3/view",
                    mimeType = "application/zip",
                    subject = subject,
                    department = department,
                    semester = semester.removePrefix("SEMESTER-").toIntOrNull() ?: 1,
                    type = "Sample Project"
                )
            )
        }
    }
    
    private fun formatFileSize(sizeBytes: Long?): String {
        if (sizeBytes == null || sizeBytes == 0L) return ""
        
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1.0 -> "%.1f GB".format(gb)
            mb >= 1.0 -> "%.1f MB".format(mb)
            kb >= 1.0 -> "%.1f KB".format(kb)
            else -> "$sizeBytes B"
        }
    }
}
