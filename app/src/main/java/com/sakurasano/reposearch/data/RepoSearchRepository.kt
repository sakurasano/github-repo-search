package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.GitHubRepo
import javax.inject.Inject

/**
 * GitHubのリポジトリ検索を行うデータソース
 *
 * @param query 検索キーワード（GitHub検索構文が使える。例: compose language:kotlin）
 * @return リポジトリ一覧。ヒット0件なら空リスト。
 */
interface RepoSearchRepository {
    suspend fun searchRepositories(query: String): List<GitHubRepo>
}

class RepoSearchRepositoryImpl @Inject constructor(
    private val api: GitHubApi,
) : RepoSearchRepository {

    override suspend fun searchRepositories(query: String): List<GitHubRepo> =
        api.searchRepositories(query).items.map { it.toDomain() }
}
