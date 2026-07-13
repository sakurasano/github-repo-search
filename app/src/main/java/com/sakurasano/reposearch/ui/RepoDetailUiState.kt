package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.RepoDetail

sealed interface RepoDetailUiState {
    data object Loading : RepoDetailUiState
    data class Success(val repo: RepoDetail) : RepoDetailUiState
    data class Error(val error: AppError) : RepoDetailUiState
}
