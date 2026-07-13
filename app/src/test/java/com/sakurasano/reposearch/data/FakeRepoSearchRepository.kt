package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSummary

class FakeRepoSearchRepository(
    private val result: DataResult<List<RepoSummary>>,
) : RepoSearchRepository {
    override suspend fun searchRepositories(query: String): DataResult<List<RepoSummary>> = result
}
