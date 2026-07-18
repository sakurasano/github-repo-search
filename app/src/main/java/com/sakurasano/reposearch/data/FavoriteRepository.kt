package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.data.local.FavoriteDao
import com.sakurasano.reposearch.data.local.FavoriteRepoEntity
import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val READ_RETRY_ATTEMPTS = 3L

interface FavoriteRepository {
    val favorites: Flow<DataResult<List<RepoSummary>>>

    val favoriteIds: Flow<Set<Long>>

    fun observeIsFavorite(id: Long): Flow<Boolean>

    suspend fun toggle(repo: RepoSummary): DataResult<Unit>

    suspend fun remove(id: Long): DataResult<Unit>
}

class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao,
) : FavoriteRepository {

    private val toggleMutex = Mutex()

    // 一覧はError表示が要るのでDataResultで返し、★判定は無音でフォールバックする
    override val favorites: Flow<DataResult<List<RepoSummary>>> =
        dao.observeAll()
            .map<List<FavoriteRepoEntity>, DataResult<List<RepoSummary>>> { entities ->
                DataResult.Success(entities.map { it.toDomain() })
            }
            .retryReads()
            .catch { emit(DataResult.Failure(AppError.Unknown(it))) }

    override val favoriteIds: Flow<Set<Long>> =
        dao.observeFavoriteIds()
            .map { it.toSet() }
            .retryReads()
            .catch { emit(emptySet()) }

    override fun observeIsFavorite(id: Long): Flow<Boolean> =
        dao.observeIsFavorite(id)
            .retryReads()
            .catch { emit(false) }

    // 連打しても取り違えないよう、実DBの登録有無を都度確認して1件ずつ順に切り替える
    override suspend fun toggle(repo: RepoSummary): DataResult<Unit> = toggleMutex.withLock {
        dbCall {
            if (dao.existsById(repo.id)) {
                dao.deleteById(repo.id)
            } else {
                dao.insert(repo.toFavoriteEntity(savedAt = System.currentTimeMillis()))
            }
        }
    }

    override suspend fun remove(id: Long): DataResult<Unit> =
        dbCall { dao.deleteById(id) }
}

// 一時的な読み取り失敗を数回まで再購読して自己回復させる
private fun <T> Flow<T>.retryReads(): Flow<T> =
    retryWhen { cause, attempt -> cause !is CancellationException && attempt < READ_RETRY_ATTEMPTS }
