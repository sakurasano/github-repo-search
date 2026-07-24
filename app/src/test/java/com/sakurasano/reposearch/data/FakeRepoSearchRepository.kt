package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSearchPage
import com.sakurasano.reposearch.model.SearchSort

class FakeRepoSearchRepository(
    private val result: DataResult<RepoSearchPage>,
) : RepoSearchRepository {
    val requests = mutableListOf<SearchRequest>()

    override suspend fun searchRepositories(query: String, page: Int, sort: SearchSort): DataResult<RepoSearchPage> {
        requests.add(SearchRequest(query, page, sort))
        return result
    }
}

data class SearchRequest(val query: String, val page: Int, val sort: SearchSort)
