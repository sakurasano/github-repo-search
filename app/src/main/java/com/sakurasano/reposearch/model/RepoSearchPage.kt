package com.sakurasano.reposearch.model

/**
 * 検索結果の1ページ分。[hasMore] は続きのページを取得できるかを表す。
 * [totalCount] はページングによらず一致する検索全体の総件数。
 */
data class RepoSearchPage(
    val items: List<RepoSummary>,
    val hasMore: Boolean,
    val totalCount: Int = 0,
)
