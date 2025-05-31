package com.noobdev.numlexambuddy.data

        import com.google.api.services.drive.Drive
        import com.noobdev.numlexambuddy.Screens.Paper
        import kotlinx.coroutines.Dispatchers
        import kotlinx.coroutines.withContext

        class PastPapersRepository(private val driveServiceManager: DriveServiceManager) {
            // Use the folder ID from Google Drive
            private val rootFolderId = "1wnOHvbCCBYTOLvfL6UJjzQdenkQ4Bk8r"

            // Cache for folder structures to minimize API calls
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

            suspend fun getPastPapers(degreeProgram: String, semester: String, subject: String): List<Paper> = withContext(Dispatchers.IO) {
                try {
                    val driveService = driveServiceManager.getDriveService()
                    val degreeFolderId = getFolderId(rootFolderId, degreeProgram)
                        ?: throw FolderNotFoundException("Department folder '$degreeProgram' not found")
                    val semesterFolderId = getFolderId(degreeFolderId, semester)
                        ?: throw FolderNotFoundException("Semester folder '$semester' not found")
                    val subjectFolderId = getFolderId(semesterFolderId, subject)
                        ?: throw FolderNotFoundException("Subject folder '$subject' not found")// Try to find the "pastpaper" folder (based on drive structure)
                    val pastPapersFolderId = try {
                        getFolderId(subjectFolderId, "pastpaper")
                            ?: getFolderId(subjectFolderId, "past papers")  // fallback
                            ?: getFolderId(subjectFolderId, "Past Papers")  // fallback
                            ?: subjectFolderId  // If no specific past papers folder, use subject folder
                    } catch (e: Exception) {
                        subjectFolderId  // Default to subject folder
                    }

                    val result = driveService.files().list()
                        .setQ("'$pastPapersFolderId' in parents and mimeType!='application/vnd.google-apps.folder' and trashed=false")
                        .setSpaces("drive")
                        .setFields("files(id, name, webViewLink)")
                        .execute()

                    val semNumber = extractSemesterNumber(semester)
                    return@withContext result.files.map { file ->
                        Paper(
                            title = extractExamType(file.name),
                            subject = subject,
                            semester = semNumber,
                            year = extractYear(file.name),
                            department = degreeProgram,
                            fileId = file.id,
                            downloadUrl = file.webViewLink ?: ""
                        )
                    }.also { papers ->
                        if (papers.isEmpty()) {
                            throw Exception("No past papers found for $subject in $degreeProgram $semester")
                        }
                    }
                } catch (e: FolderNotFoundException) {
                    throw e
                } catch (e: Exception) {
                    throw Exception("Failed to get past papers for $subject: ${e.message}")
                }
            }

            // Helper method to get a folder ID by name
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
            }            // Helper to extract semester number from folder name
            private fun extractSemesterNumber(semesterName: String): Int {
                val regex = Regex("SEMESTER-?(\\d+)", RegexOption.IGNORE_CASE)
                val matchResult = regex.find(semesterName)
                return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0
            }

            // Helper to extract exam type from filename
            private fun extractExamType(fileName: String): String {
                return when {
                    fileName.contains("midterm", ignoreCase = true) -> "Midterm Exam"
                    fileName.contains("final", ignoreCase = true) -> "Final Exam"
                    fileName.contains("quiz", ignoreCase = true) -> "Quiz"
                    fileName.contains("assignment", ignoreCase = true) -> "Assignment"
                    else -> fileName.substringBeforeLast(".")
                }
            }

            // Helper to extract year from filename
            private fun extractYear(fileName: String): String {
                val yearRegex = "20\\d{2}".toRegex()
                return yearRegex.find(fileName)?.value ?: "Unknown"
            }

            class FolderNotFoundException(folderName: String) : Exception("Folder not found: $folderName")
        }