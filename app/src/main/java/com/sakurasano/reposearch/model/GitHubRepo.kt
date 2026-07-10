package com.sakurasano.reposearch.model

data class GitHubRepo(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String,
    val ownerName: String,
    val ownerAvatarUrl: String,
    val starCount: Int,
    val forkCount: Int,
    val language: String,
    val topics: List<String>,
    val openIssueCount: Int,
    val htmlUrl: String,
    val license: String,
)
