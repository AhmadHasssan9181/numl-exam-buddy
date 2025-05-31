package com.noobdev.numlexambuddy.data

import com.google.api.services.drive.Drive
import com.noobdev.numlexambuddy.model.Lecture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LecturesRepository(private val driveServiceManager: DriveServiceManager) {
    private val rootFolderId = "1wnOHvbCCBYTOLvfL6UJjzQdenkQ4Bk8r"
    private val folderCache = mutableMapOf<String, String>()
    
    suspend fun getSemesters(degreeProgram: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val driveService = driveServiceManager.getDriveService()
            val degreeFolderId = getFolderId(rootFolderId, degreeProgram)
                ?: throw FolderNotFoundException("Department folder '$degreeProgram' not found")

            val result = driveService.files().list()
                .setQ("'$degreeFolderId' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val semesters = result.files.map { it.name }.sorted()
            if (semesters.isEmpty()) {
                throw Exception("No semesters found for $degreeProgram")
            }
            return@withContext semesters
        } catch (e: FolderNotFoundException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to get semesters for $degreeProgram: ${e.message}")
        }
    }
    suspend fun getSubjects(degreeProgram: String, semester: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val driveService = driveServiceManager.getDriveService()
            val degreeFolderId = getFolderId(rootFolderId, degreeProgram)
                ?: throw FolderNotFoundException("Department folder '$degreeProgram' not found")
            val semesterFolderId = getFolderId(degreeFolderId, semester)
                ?: throw FolderNotFoundException("Semester folder '$semester' not found in $degreeProgram")

            val result = driveService.files().list()
                .setQ("'$semesterFolderId' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val subjects = result.files.map { it.name }.sorted()
            if (subjects.isEmpty()) {
                throw Exception("No subjects found for $degreeProgram $semester")
            }
            return@withContext subjects
        } catch (e: FolderNotFoundException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to get subjects for $degreeProgram $semester: ${e.message}")
        }
    }
    suspend fun getLectures(degreeProgram: String, semester: String, subject: String): List<Lecture> = withContext(Dispatchers.IO) {
        try {
            val driveService = driveServiceManager.getDriveService()
            val degreeFolderId = getFolderId(rootFolderId, degreeProgram)
                ?: throw FolderNotFoundException("Department folder '$degreeProgram' not found")
            val semesterFolderId = getFolderId(degreeFolderId, semester)
                ?: throw FolderNotFoundException("Semester folder '$semester' not found")
            val subjectFolderId = getFolderId(semesterFolderId, subject)
                ?: throw FolderNotFoundException("Subject folder '$subject' not found")

            // Try to find the "Slides" folder (based on drive structure)
            val slidesFolderId = try {
                getFolderId(subjectFolderId, "Slides")
                    ?: getFolderId(subjectFolderId, "slides")  // fallback
                    ?: getFolderId(subjectFolderId, "SLIDES")  // fallback
                    ?: subjectFolderId  // If no specific slides folder, use subject folder
            } catch (e: Exception) {
                subjectFolderId  // Default to subject folder
            }

            val result = driveService.files().list()
                .setQ("'$slidesFolderId' in parents and mimeType!='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name, webViewLink)")
                .execute()

            val semNumber = extractSemesterNumber(semester)
            val lectures = result.files.map { file ->
                Lecture(
                    title = extractLectureTitle(file.name),
                    subject = subject,
                    semester = semNumber,
                    type = "Slides",
                    department = degreeProgram,
                    fileId = file.id,
                    downloadUrl = file.webViewLink ?: ""
                )
            }

            if (lectures.isEmpty()) {
                throw Exception("No lecture slides found for $subject in $degreeProgram $semester")
            }
            return@withContext lectures
        } catch (e: FolderNotFoundException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to get lectures for $subject: ${e.message}")
        }
    }    // Helper method to get a folder ID by name
    private suspend fun getFolderId(parentId: String, folderName: String): String? {
        // Check cache first
        val cacheKey = "$parentId:$folderName"
        folderCache[cacheKey]?.let { return it }

        val driveService = driveServiceManager.getDriveService()
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
    }

    // Helper to extract semester number from folder name
    private fun extractSemesterNumber(semesterName: String): Int {
        val regex = "SEMESTER-?(\\d+)".toRegex()
        val matchResult = regex.find(semesterName.uppercase())
        return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    // Helper to extract lecture title from filename
    private fun extractLectureTitle(fileName: String): String {
        return when {
            fileName.contains("lecture", ignoreCase = true) -> "Lecture - ${fileName.substringBeforeLast(".")}"
            fileName.contains("slides", ignoreCase = true) -> "Slides - ${fileName.substringBeforeLast(".")}"
            fileName.contains("presentation", ignoreCase = true) -> "Presentation - ${fileName.substringBeforeLast(".")}"
            else -> fileName.substringBeforeLast(".")
        }
    }

    class FolderNotFoundException(folderName: String) : Exception("Folder not found: $folderName")
}
