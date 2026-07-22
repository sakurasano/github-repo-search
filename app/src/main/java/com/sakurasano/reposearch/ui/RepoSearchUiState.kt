package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.RepoSummary

sealed interface RepoSearchUiState {
    data object Idle : RepoSearchUiState // 検索前
    data object Loading : RepoSearchUiState
    data object Empty : RepoSearchUiState // 検索は成功したが結果が0件
    data class Success(
        val repos: List<RepoSummary>,
        val loadMoreState: LoadMoreState = LoadMoreState.Idle,
    ) : RepoSearchUiState

    data class Error(val error: AppError) : RepoSearchUiState
}
