package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSearchPage
import com.sakurasano.reposearch.model.SearchSort

class FakeRepoSearchRepository(
    private val result: DataResult<RepoSearchPage>,
) : RepoSearchRepository {
    val requests = mutableListOf<SearchRequest>()

    override suspend fun searchRepositories(query: String, sort: SearchSort, page: Int): DataResult<RepoSearchPage> {
        requests.add(SearchRequest(query, sort, page))
        return result
    }
}

data class SearchRequest(val query: String, val sort: SearchSort, val page: Int)
