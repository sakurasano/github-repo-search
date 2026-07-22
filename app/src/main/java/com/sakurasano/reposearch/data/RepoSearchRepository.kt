package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSearchPage
import javax.inject.Inject

interface RepoSearchRepository {
    /**
     * @param query 検索キーワード（GitHub検索構文が使える。例: "compose language:kotlin"）
     * @param page 取得するページ番号（1始まり）
     */
    suspend fun searchRepositories(query: String, page: Int): DataResult<RepoSearchPage>
}

class RepoSearchRepositoryImpl @Inject constructor(
    private val api: GitHubApi,
) : RepoSearchRepository {

    override suspend fun searchRepositories(query: String, page: Int): DataResult<RepoSearchPage> =
        apiCall {
            val response = api.searchRepositories(query, page)
            RepoSearchPage(
                items = response.items.map { it.toDomain() },
                hasMore = hasMorePages(page, GitHubApi.DEFAULT_PER_PAGE, response.totalCount),
            )
        }
}

// GitHub検索APIは合計最大1000件までしか返さず、それを超えるページ要求はエラーになる
private const val MAX_SEARCH_RESULTS = 1000

/**
 * [page] まで取得した時点で、さらに続きのページを取得できるかを返す。
 * 取得できるのは総件数と[MAX_SEARCH_RESULTS]のうち小さい方の件数までに限られる。
 */
internal fun hasMorePages(page: Int, perPage: Int, totalCount: Int): Boolean =
    page * perPage < minOf(totalCount, MAX_SEARCH_RESULTS)
