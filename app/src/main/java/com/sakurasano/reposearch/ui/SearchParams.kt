package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.model.SearchSort

/**
 * 確定した検索条件。
 */
data class SearchParams(
    val query: String,
    val sort: SearchSort,
)
