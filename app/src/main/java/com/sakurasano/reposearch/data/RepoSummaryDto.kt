package com.sakurasano.reposearch.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoSearchResponseDto(
    val items: List<RepoSummaryDto> = emptyList(),
)

@Serializable
data class RepoSummaryDto(
    val id: Long,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val description: String? = null,
    val owner: OwnerDto? = null,
    @SerialName("stargazers_count") val starCount: Int,
    val language: String? = null,
)

@Serializable
data class OwnerDto(
    val login: String,
)

@Serializable
data class LicenseDto(
    val name: String,
)
