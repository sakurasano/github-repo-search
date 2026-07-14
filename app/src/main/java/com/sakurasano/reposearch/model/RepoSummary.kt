package com.sakurasano.reposearch.model

/**
 * 検索結果一覧で表示するリポジトリの要約モデル。
 */
data class RepoSummary(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String,
    val ownerName: String,
    val ownerAvatarUrl: String,
    val starCount: Int,
    val language: String,
)
