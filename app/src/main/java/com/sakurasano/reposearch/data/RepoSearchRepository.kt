package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.GitHubRepo
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

interface RepoSearchRepository {
    /**
     * @param query 検索キーワード（GitHub検索構文が使える。例: "compose language:kotlin"）
     */
    suspend fun searchRepositories(query: String): DataResult<List<GitHubRepo>>
}

class RepoSearchRepositoryImpl @Inject constructor(
    private val api: GitHubApi,
) : RepoSearchRepository {

    override suspend fun searchRepositories(query: String): DataResult<List<GitHubRepo>> =
        try {
            val repos = api.searchRepositories(query).items.map { it.toDomain() }
            DataResult.Success(repos)
        } catch (e: CancellationException) {
            throw e // キャンセルは飲み込まず伝播させる
        } catch (e: IOException) {
            DataResult.Failure(AppError.Network)
        } catch (e: HttpException) {
            val error = if (e.code() == 403) AppError.RateLimited else AppError.Server(e.code())
            DataResult.Failure(error)
        } catch (e: Exception) {
            DataResult.Failure(AppError.Unknown(e))
        }
}
