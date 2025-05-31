package com.noobdev.numlexambuddy.model

/**
 * Shared data models for the NUML Exam Buddy app
 */

data class Department(val name: String, val code: String)

data class Paper(
    val title: String,
    val subject: String,
    val semester: Int,
    val year: String,
    val department: String,
    val fileId: String = "",
    val downloadUrl: String = ""
)

data class Lecture(
    val title: String,
    val subject: String,
    val semester: Int,
    val type: String, // "Slides", "Video", etc.
    val department: String,
    val fileId: String = "",
    val downloadUrl: String = ""
)

data class StudyMaterial(
    val title: String,
    val subject: String,
    val semester: Int,
    val type: String, // "PDF Notes", "Word Document", etc.
    val department: String,
    val fileId: String = "",
    val downloadUrl: String = "",
    val size: String = "" // File size information
)

data class Project(
    val id: String,
    val name: String,
    val size: String,
    val downloadUrl: String,
    val mimeType: String,
    val subject: String = "",
    val semester: Int = 0,
    val department: String = "",
    val type: String = "Project"
)
