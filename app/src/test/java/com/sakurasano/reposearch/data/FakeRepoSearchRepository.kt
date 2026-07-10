package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.GitHubRepo

class FakeRepoSearchRepository(
    private val result: DataResult<List<GitHubRepo>>,
) : RepoSearchRepository {
    override suspend fun searchRepositories(query: String): DataResult<List<GitHubRepo>> = result
}
