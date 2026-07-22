package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSearchPage

class FakeRepoSearchRepository(
    private val result: DataResult<RepoSearchPage>,
) : RepoSearchRepository {
    override suspend fun searchRepositories(query: String, page: Int): DataResult<RepoSearchPage> = result
}
