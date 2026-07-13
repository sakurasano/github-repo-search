package com.sakurasano.reposearch.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {

    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String? = null, // 指定しない場合、Defaultはbest match。参考：https://docs.github.com/ja/rest/search/search?apiVersion=2026-03-10#ranking-search-results
        @Query("order") order: String? = DEFAULT_ORDER,
        @Query("per_page") perPage: Int? = DEFAULT_PER_PAGE,
    ): RepoSearchResponseDto

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): RepoDetailDto

    companion object {
        private const val DEFAULT_ORDER = "desc"
        private const val DEFAULT_PER_PAGE = 30
    }
}
