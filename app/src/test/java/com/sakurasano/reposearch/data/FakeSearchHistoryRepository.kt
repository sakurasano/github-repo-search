package com.sakurasano.reposearch.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSearchHistoryRepository(initial: List<String> = emptyList()) : SearchHistoryRepository {
    private val state = MutableStateFlow(initial)
    override val history: StateFlow<List<String>> = state

    override suspend fun record(query: String) {
        state.value = state.value.withRecorded(query)
    }

    override suspend fun remove(query: String) {
        state.value = state.value - query
    }

    override suspend fun clear() {
        state.value = emptyList()
    }
}
