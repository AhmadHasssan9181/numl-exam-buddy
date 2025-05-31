package com.noobdev.numlexambuddy.data

import com.google.api.services.drive.Drive
import com.noobdev.numlexambuddy.model.StudyMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyMaterialRepository(private val driveService: Drive) {
    private val rootFolderId = "1wnOHvbCCBYTOLvfL6UJjzQdenkQ4Bk8r"
    private val folderCache = mutableMapOf<String, String>()
    suspend fun getSemesters(degreeProgram: String): List<String> = withContext(Dispatchers.IO) {
        try {
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
    suspend fun getStudyMaterials(degreeProgram: String, semester: String, subject: String): List<StudyMaterial> = withContext(Dispatchers.IO) {
        try {
            val degreeFolderId = getFolderId(rootFolderId, degreeProgram)
                ?: throw FolderNotFoundException("Department folder '$degreeProgram' not found")
            val semesterFolderId = getFolderId(degreeFolderId, semester)
                ?: throw FolderNotFoundException("Semester folder '$semester' not found")
            val subjectFolderId = getFolderId(semesterFolderId, subject)
                ?: throw FolderNotFoundException("Subject folder '$subject' not found")

            // Try to find the "Notes" folder (based on drive structure)
            val notesFolderId = try {
                getFolderId(subjectFolderId, "Notes")
                    ?: getFolderId(subjectFolderId, "notes")  // fallback
                    ?: getFolderId(subjectFolderId, "NOTES")  // fallback
                    ?: subjectFolderId  // If no specific notes folder, use subject folder
            } catch (e: Exception) {
                subjectFolderId  // Default to subject folder
            }

            val result = driveService.files().list()
                .setQ("'$notesFolderId' in parents and mimeType!='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name, webViewLink)")
                .execute()

            val semNumber = extractSemesterNumber(semester)
            val materials = result.files.map { file ->
                StudyMaterial(
                    title = extractMaterialTitle(file.name),
                    subject = subject,
                    semester = semNumber,
                    type = determineFileType(file.name),
                    department = degreeProgram,
                    fileId = file.id,
                    downloadUrl = file.webViewLink ?: ""
                )
            }

            if (materials.isEmpty()) {
                throw Exception("No study materials found for $subject in $degreeProgram $semester")
            }
            return@withContext materials
        } catch (e: FolderNotFoundException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to get study materials for $subject: ${e.message}")
        }
    }

    // Helper method to get a folder ID by name
    private suspend fun getFolderId(parentId: String, folderName: String): String? {
        // Check cache first
        val cacheKey = "$parentId:$folderName"
        folderCache[cacheKey]?.let { return it }

        val driveService = this.driveService
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
        val regex = Regex("SEMESTER-?(\\d+)", RegexOption.IGNORE_CASE)
        val matchResult = regex.find(semesterName)
        return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    // Helper to extract material title from filename
    private fun extractMaterialTitle(fileName: String): String {
        return when {
            fileName.contains("notes", ignoreCase = true) -> "Notes - ${fileName.substringBeforeLast(".")}"
            fileName.contains("summary", ignoreCase = true) -> "Summary - ${fileName.substringBeforeLast(".")}"
            fileName.contains("handout", ignoreCase = true) -> "Handout - ${fileName.substringBeforeLast(".")}"
            fileName.contains("material", ignoreCase = true) -> "Study Material - ${fileName.substringBeforeLast(".")}"
            else -> fileName.substringBeforeLast(".")
        }
    }

    // Helper to determine file type from extension
    private fun determineFileType(fileName: String): String {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return when (extension) {
            "pdf" -> "PDF Notes"
            "docx", "doc" -> "Word Document"
            "pptx", "ppt" -> "Presentation"
            "txt" -> "Text Notes"
            "md" -> "Markdown Notes"
            else -> "Study Material"
        }
    }

    class FolderNotFoundException(folderName: String) : Exception("Folder not found: $folderName")
}
