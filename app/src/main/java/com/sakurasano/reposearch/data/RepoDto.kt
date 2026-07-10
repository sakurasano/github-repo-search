package com.sakurasano.reposearch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoSearchResponseDto(
    val items: List<RepoDto> = emptyList(),
)

@Serializable
data class RepoDto(
    val id: Long,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val description: String? = null,
    val owner: OwnerDto? = null,
    @SerialName("stargazers_count") val starCount: Int,
    @SerialName("forks_count") val forkCount: Int,
    val language: String? = null,
    val topics: List<String>? = null,
    @SerialName("open_issues_count") val openIssueCount: Int,
    @SerialName("html_url") val htmlUrl: String,
    val license: LicenseDto? = null,
)

@Serializable
data class OwnerDto(
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String,
)

@Serializable
data class LicenseDto(
    val name: String,
)
