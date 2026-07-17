package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.RepoSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeFavoriteRepository(initial: List<RepoSummary> = emptyList()) : FavoriteRepository {
    private val state = MutableStateFlow(initial)

    // trueにするとadd/removeが例外を投げ、書き込み失敗を再現できる
    var failWrites: Boolean = false

    // trueにするとfavoritesの読み取りが例外を投げ、読み取り失敗を再現できる
    var failReads: Boolean = false

    override val favorites: Flow<List<RepoSummary>> = flow {
        if (failReads) throw RuntimeException("read failed")
        emitAll(state)
    }

    override val favoriteIds: Flow<Set<Long>> = state.map { list -> list.map { it.id }.toSet() }

    override fun observeIsFavorite(id: Long): Flow<Boolean> =
        state.map { list -> list.any { it.id == id } }

    override suspend fun add(repo: RepoSummary) {
        if (failWrites) throw RuntimeException("insert failed")
        if (state.value.none { it.id == repo.id }) {
            state.value = listOf(repo) + state.value
        }
    }

    override suspend fun remove(id: Long) {
        if (failWrites) throw RuntimeException("delete failed")
        state.value = state.value.filterNot { it.id == id }
    }
}
