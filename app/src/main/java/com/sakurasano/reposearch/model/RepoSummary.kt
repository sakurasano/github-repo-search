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

// 詳細画面からのお気に入り操作を RepoSummary 経路へ一本化するための変換
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
