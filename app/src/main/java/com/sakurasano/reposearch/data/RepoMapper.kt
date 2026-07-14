package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.RepoDetail
import com.sakurasano.reposearch.model.RepoSummary

fun RepoSummaryDto.toDomain(): RepoSummary = RepoSummary(
    id = id,
    name = name,
    fullName = fullName,
    description = description.orEmpty(),
    ownerName = owner?.login.orEmpty(),
    ownerAvatarUrl = owner?.avatarUrl.orEmpty(),
    starCount = starCount,
    language = language.orEmpty(),
)

fun RepoDetailDto.toDomain(): RepoDetail = RepoDetail(
    name = name,
    fullName = fullName,
    description = description.orEmpty(),
    ownerName = owner?.login.orEmpty(),
    ownerAvatarUrl = owner?.avatarUrl.orEmpty(),
    starCount = starCount,
    forkCount = forkCount,
    openIssueCount = openIssueCount,
    language = language.orEmpty(),
    topics = topics.orEmpty(),
    license = license?.name.orEmpty(),
)
