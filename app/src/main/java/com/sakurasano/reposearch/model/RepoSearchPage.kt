package com.sakurasano.reposearch.model

/**
 * 検索結果の1ページ分。
 */
data class RepoSearchPage(
    val items: List<RepoSummary>,
    val hasMore: Boolean, // 続きのページを取得できるか
    val totalCount: Int = 0, // ページングによらず一致する検索全体の総件数
)
