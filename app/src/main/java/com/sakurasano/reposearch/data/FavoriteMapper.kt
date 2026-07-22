package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.data.local.FavoriteRepoEntity
import com.sakurasano.reposearch.model.RepoDetail
import com.sakurasano.reposearch.model.RepoSummary

fun RepoSummary.toFavoriteEntity(savedAt: Long): FavoriteRepoEntity = FavoriteRepoEntity(
    id = id,
    name = name,
    fullName = fullName,
    description = description,
    ownerName = ownerName,
    ownerAvatarUrl = ownerAvatarUrl,
    starCount = starCount,
    language = language,
    savedAt = savedAt,
)

fun FavoriteRepoEntity.toDomain(): RepoSummary = RepoSummary(
    id = id,
    name = name,
    fullName = fullName,
    description = description,
    ownerName = ownerName,
    ownerAvatarUrl = ownerAvatarUrl,
    starCount = starCount,
    language = language,
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
