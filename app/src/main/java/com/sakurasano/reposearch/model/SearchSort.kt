package com.sakurasano.reposearch.model

/**
 * リポジトリ検索の並び順。[BEST_MATCH] はGitHubの既定で、関連度順になる。
 */
enum class SearchSort {
    BEST_MATCH,
    STARS,
    UPDATED,
}
