package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.SearchSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSearchSortRepository(initial: SearchSort = SearchSort.BEST_MATCH) : SearchSortRepository {
    private val state = MutableStateFlow(initial)
    override val sortOption: Flow<SearchSort> = state

    override suspend fun setSortOption(sort: SearchSort) {
        state.value = sort
    }
}
