package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.data.local.FavoriteDao
import com.sakurasano.reposearch.model.RepoSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface FavoriteRepository {
    val favorites: Flow<List<RepoSummary>>

    val favoriteIds: Flow<Set<Long>>

    fun observeIsFavorite(id: Long): Flow<Boolean>

    suspend fun add(repo: RepoSummary)

    suspend fun remove(id: Long)
}

class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao,
) : FavoriteRepository {

    // 読み取り失敗はここで握りつぶさず流し、表示(Error/フォールバック)の判断はUI層に委ねる
    override val favorites: Flow<List<RepoSummary>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override val favoriteIds: Flow<Set<Long>> =
        dao.observeFavoriteIds().map { it.toSet() }

    override fun observeIsFavorite(id: Long): Flow<Boolean> = dao.observeIsFavorite(id)

    override suspend fun add(repo: RepoSummary) {
        dao.insert(repo.toFavoriteEntity(savedAt = System.currentTimeMillis()))
    }

    override suspend fun remove(id: Long) {
        dao.deleteById(id)
    }
}
