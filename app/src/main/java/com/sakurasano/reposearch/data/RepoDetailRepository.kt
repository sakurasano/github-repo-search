package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoDetail
import javax.inject.Inject

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
        apiCall { api.getRepository(owner, name).toDomain() }
}
