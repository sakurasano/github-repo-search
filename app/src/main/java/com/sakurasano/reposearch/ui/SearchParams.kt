package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.model.SearchSort

/**
 * 確定した検索条件。一覧のスクロール位置はこれを識別子として保持/リセットされる
 */
data class SearchParams(
    val query: String,
    val sort: SearchSort,
)
