package com.sakurasano.reposearch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoDetailDto(
    val name: String,
    @SerialName("full_name") val fullName: String,
    val description: String? = null,
    val owner: OwnerDto? = null,
    @SerialName("stargazers_count") val starCount: Int,
    @SerialName("forks_count") val forkCount: Int,
    @SerialName("open_issues_count") val openIssueCount: Int,
    val language: String? = null,
    val topics: List<String>? = null,
    val license: LicenseDto? = null,
)
