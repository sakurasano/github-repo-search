package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSummary
import javax.inject.Inject

interface RepoSearchRepository {
    /**
     * @param query 検索キーワード（GitHub検索構文が使える。例: "compose language:kotlin"）
     */
    suspend fun searchRepositories(query: String): DataResult<List<RepoSummary>>
}

class RepoSearchRepositoryImpl @Inject constructor(
    private val api: GitHubApi,
) : RepoSearchRepository {

    override suspend fun searchRepositories(query: String): DataResult<List<RepoSummary>> =
        apiCall { api.searchRepositories(query).items.map { it.toDomain() } }
}
