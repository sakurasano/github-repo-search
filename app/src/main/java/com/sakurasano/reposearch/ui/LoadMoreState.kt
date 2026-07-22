package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.model.AppError

/**
 * 検索結果一覧末尾での追加読み込みの状態。一覧本体（[RepoSearchUiState.Success]）を表示したまま従属的に遷移する。
 */
sealed interface LoadMoreState {
    data object Idle : LoadMoreState // 追加読み込み可能・待機
    data object Loading : LoadMoreState
    data class Error(val error: AppError) : LoadMoreState
    data object End : LoadMoreState // これ以上のページはない

    companion object {
        fun from(hasMore: Boolean): LoadMoreState = if (hasMore) Idle else End
    }
}
