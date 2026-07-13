package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoDetail

class FakeRepoDetailRepository(
    var result: DataResult<RepoDetail>,
) : RepoDetailRepository {
    override suspend fun getRepository(owner: String, name: String): DataResult<RepoDetail> = result
}
