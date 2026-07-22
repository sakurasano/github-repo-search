package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeFavoriteRepository(initial: List<RepoSummary> = emptyList()) : FavoriteRepository {
    private val state = MutableStateFlow(initial)

    // trueにするとadd/removeが失敗(Failure)を返し、書き込み失敗を再現できる
    var failWrites: Boolean = false

    // trueにするとfavoritesが失敗(Failure)を流し、読み取り失敗を再現できる
    var failReads: Boolean = false

    override val favorites: Flow<DataResult<List<RepoSummary>>> = flow {
        if (failReads) {
            emit(DataResult.Failure(AppError.Unknown(RuntimeException("read failed"))))
            return@flow
        }
        emitAll(state.map { DataResult.Success(it) })
    }

    override val favoriteIds: Flow<Set<Long>> = state.map { list -> list.map { it.id }.toSet() }

    override fun observeIsFavorite(id: Long): Flow<Boolean> =
        state.map { list -> list.any { it.id == id } }

    override suspend fun toggle(repo: RepoSummary): DataResult<Unit> {
        if (failWrites) return DataResult.Failure(AppError.Unknown(RuntimeException("toggle failed")))
        state.value = if (state.value.any { it.id == repo.id }) {
            state.value.filterNot { it.id == repo.id }
        } else {
            listOf(repo) + state.value
        }
        return DataResult.Success(Unit)
    }

    override suspend fun remove(id: Long): DataResult<Unit> {
        if (failWrites) return DataResult.Failure(AppError.Unknown(RuntimeException("delete failed")))
        state.value = state.value.filterNot { it.id == id }
        return DataResult.Success(Unit)
    }
}
