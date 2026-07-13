package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoDetail
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

interface RepoDetailRepository {
    /**
     * @param owner リポジトリのオーナー名
     * @param name リポジトリ名
     */
    suspend fun getRepository(owner: String, name: String): DataResult<RepoDetail>
}

class RepoDetailRepositoryImpl @Inject constructor(
    private val api: GitHubApi,
) : RepoDetailRepository {

    override suspend fun getRepository(owner: String, name: String): DataResult<RepoDetail> =
        try {
            DataResult.Success(api.getRepository(owner, name).toDomain())
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
