package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.model.RepoSummary

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState // お気に入りが0件
    data class Success(val repos: List<RepoSummary>) : FavoritesUiState
    data object Error : FavoritesUiState // 読み取りに失敗
}
