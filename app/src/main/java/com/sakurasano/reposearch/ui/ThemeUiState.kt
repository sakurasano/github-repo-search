package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.model.ThemeMode

sealed interface ThemeUiState {
    data object Loading : ThemeUiState // DataStoreの初回値が届くまで
    data class Success(val themeMode: ThemeMode) : ThemeUiState
}
