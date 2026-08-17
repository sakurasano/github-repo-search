package com.sakurasano.reposearch.model

/**
 * リポジトリ詳細画面で表示するモデル。
 */
data class RepoDetail(
    val id: Long,
    val name: String,
    val fullName: String,
    val htmlUrl: String,
    val description: String,
    val ownerName: String,
    val ownerAvatarUrl: String,
    val starCount: Int,
    val forkCount: Int,
    val openIssueCount: Int,
    val language: String,
    val topics: List<String>,
    val license: String,
)

fun RepoDetail.toSummary(): RepoSummary = RepoSummary(
    id = id,
    name = name,
    fullName = fullName,
    description = description,
    ownerName = ownerName,
    ownerAvatarUrl = ownerAvatarUrl,
    starCount = starCount,
    language = language,
)
