package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.GitHubRepo

fun RepoDto.toDomain(): GitHubRepo = GitHubRepo(
    id = id,
    name = name,
    fullName = fullName,
    description = description.orEmpty(),
    ownerName = owner?.login.orEmpty(),
    ownerAvatarUrl = owner?.avatarUrl.orEmpty(),
    starCount = starCount,
    forkCount = forkCount,
    language = language.orEmpty(),
    topics = topics.orEmpty(),
    openIssueCount = openIssueCount,
    htmlUrl = htmlUrl,
    license = license?.name.orEmpty(),
)
