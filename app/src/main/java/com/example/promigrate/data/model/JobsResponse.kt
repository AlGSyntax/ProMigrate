package com.example.promigrate.data.model

data class JobsResponse(
    val jobs: List<Job>,
    val totalResults: Int,
    val pageNumber: Int,
    val pageSize: Int
)

data class Job(
    val id: String,
    val title: String,
    val location: String,
    val employer: String,
    val description: String,
    val publicationDate: String, // oder als Date, wenn du ein Parsing durchführst
    val contractType: String?,
    val field: String?,
    val requiredSkills: List<String>?,
    val salary: String?,
    val applyUrl: String
)

