package com.noobdev.numlexambuddy.data

import com.google.api.services.drive.Drive
import com.noobdev.numlexambuddy.Screens.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProjectsRepository(private val driveService: Drive) {
    
    suspend fun getDepartments(): List<String> = withContext(Dispatchers.IO) {
        try {
            val results = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and name contains 'Department'")
                .setFields("files(id, name)")
                .execute()
            
            results.files?.map { it.name }?.sorted() ?: emptyList()
        } catch (e: Exception) {
            throw Exception("Failed to load departments: ${e.message}")
        }
    }
    
    suspend fun getSemesters(department: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // First find the department folder
            val departmentResults = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and name='$department'")
                .setFields("files(id)")
                .execute()
            
            val departmentId = departmentResults.files?.firstOrNull()?.id
                ?: throw Exception("Department folder not found")
            
            // Then find semester folders within the department
            val semesterResults = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and parents in '$departmentId' and name contains 'SEMESTER-'")
                .setFields("files(id, name)")
                .execute()
            
            semesterResults.files?.map { it.name }?.sorted() ?: emptyList()
        } catch (e: Exception) {
            throw Exception("Failed to load semesters for $department: ${e.message}")
        }
    }
    
    suspend fun getSubjects(department: String, semester: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Find department folder
            val departmentResults = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and name='$department'")
                .setFields("files(id)")
                .execute()
            
            val departmentId = departmentResults.files?.firstOrNull()?.id
                ?: throw Exception("Department folder not found")
            
            // Find semester folder
            val semesterResults = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and parents in '$departmentId' and name='$semester'")
                .setFields("files(id)")
                .execute()
            
            val semesterId = semesterResults.files?.firstOrNull()?.id
                ?: throw Exception("Semester folder not found")
            
            // Find subject folders within the semester
            val subjectResults = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and parents in '$semesterId'")
                .setFields("files(id, name)")
                .execute()
            
            subjectResults.files?.map { it.name }?.sorted() ?: emptyList()
        } catch (e: Exception) {
            throw Exception("Failed to load subjects for $department -> $semester: ${e.message}")
        }
    }
    
    suspend fun getProjects(department: String, semester: String, subject: String):
            List<Project> = withContext(Dispatchers.IO) {
        try {
            // Navigate through: Department -> Semester -> Subject -> projects
            val departmentResults = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and name='$department'")
                .setFields("files(id)")
                .execute()
            
            val departmentId = departmentResults.files?.firstOrNull()?.id
                ?: throw Exception("Department '$department' not found")
            
            val semesterResults = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and parents in '$departmentId' and name='$semester'")
                .setFields("files(id)")
                .execute()
            
            val semesterId = semesterResults.files?.firstOrNull()?.id
                ?: throw Exception("Semester '$semester' not found in department '$department'")
            
            val subjectResults = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and parents in '$semesterId' and name='$subject'")
                .setFields("files(id)")
                .execute()
            
            val subjectId = subjectResults.files?.firstOrNull()?.id
                ?: throw Exception("Subject '$subject' not found in semester '$semester'")
            
            // Look for projects folder
            val projectsFolderResults = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and parents in '$subjectId' and name='projects'")
                .setFields("files(id)")
                .execute()
            
            val projectsFolderId = projectsFolderResults.files?.firstOrNull()?.id
                ?: throw Exception("Projects folder not found for subject '$subject'")
            
            // Get all files in the projects folder
            val projectsResults = driveService.files().list()
                .setQ("parents in '$projectsFolderId'")
                .setFields("files(id, name, size, webViewLink, mimeType)")
                .execute()
            
            projectsResults.files?.map { file ->
                Project(
                    id = file.id,
                    name = file.name,
                    size = formatFileSize(file.getSize()),
                    downloadUrl = file.webViewLink ?: "",
                    mimeType = file.mimeType ?: ""
                )
            } ?: emptyList()
            
        } catch (e: Exception) {
            throw Exception("Failed to load projects for $subject: ${e.message}")
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
