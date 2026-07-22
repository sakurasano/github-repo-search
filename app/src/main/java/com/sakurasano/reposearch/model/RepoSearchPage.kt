package com.sakurasano.reposearch.model

/**
 * 検索結果の1ページ分。[hasMore] は続きのページを取得できるかを表す。
 */
data class RepoSearchPage(
    val items: List<RepoSummary>,
    val hasMore: Boolean,
)
