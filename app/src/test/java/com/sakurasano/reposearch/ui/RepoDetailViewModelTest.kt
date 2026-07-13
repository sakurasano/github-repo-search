package com.sakurasano.reposearch.ui

import androidx.lifecycle.SavedStateHandle
import com.sakurasano.reposearch.MainDispatcherRule
import com.sakurasano.reposearch.data.FakeRepoDetailRepository
import com.sakurasano.reposearch.data.RepoDetailRepository
import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoDetail
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RepoDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val savedStateHandle =
        SavedStateHandle(mapOf("owner" to "android", "name" to "nowinandroid"))

    @Test
    fun `詳細取得が成功するとSuccessになる`() = runTest {
        val repo = sampleDetail()
        val viewModel = RepoDetailViewModel(
            FakeRepoDetailRepository(DataResult.Success(repo)),
            savedStateHandle,
        )

        assertEquals(RepoDetailUiState.Success(repo), viewModel.uiState.value)
    }

    @Test
    fun `詳細取得が失敗するとErrorになる`() = runTest {
        val viewModel = RepoDetailViewModel(
            FakeRepoDetailRepository(DataResult.Failure(AppError.Network)),
            savedStateHandle,
        )

        assertEquals(RepoDetailUiState.Error(AppError.Network), viewModel.uiState.value)
    }

    @Test
    fun `再試行で再度取得しSuccessになる`() = runTest {
        val repo = sampleDetail()
        val repository = FakeRepoDetailRepository(DataResult.Failure(AppError.Network))
        val viewModel = RepoDetailViewModel(repository, savedStateHandle)

        // 初回は失敗
        assertEquals(RepoDetailUiState.Error(AppError.Network), viewModel.uiState.value)

        // 応答を成功に差し替えて再試行
        repository.result = DataResult.Success(repo)
        viewModel.retry()

        assertEquals(RepoDetailUiState.Success(repo), viewModel.uiState.value)
    }

    @Test
    fun `再試行中に前の取得が遅れて返っても新しい結果で上書きされない`() = runTest {
        val repo = GatedFakeDetailRepository()
        val viewModel = RepoDetailViewModel(repo, savedStateHandle) // initで1回目の取得を開始し中断

        val oldDetail = sampleDetail("old")
        val newDetail = sampleDetail("new")

        viewModel.retry() // 1回目をキャンセルして2回目を開始し、応答待ちで中断

        // あえて 新(1)→旧(0) の順で応答を返す（遅い前回=oldが後から返るケース）
        repo.complete(1, DataResult.Success(newDetail))
        repo.complete(0, DataResult.Success(oldDetail))

        // 0はキャンセル済みなので状態を上書きせず、1の結果が残る
        assertEquals(RepoDetailUiState.Success(newDetail), viewModel.uiState.value)
    }

    private fun sampleDetail(name: String = "nowinandroid") = RepoDetail(
        name = name,
        fullName = "android/$name",
        description = "説明",
        ownerName = "android",
        starCount = 100,
        forkCount = 10,
        openIssueCount = 3,
        language = "Kotlin",
        topics = listOf("compose", "android"),
        license = "Apache License 2.0",
    )
}

/**
 * 呼び出しごとに応答タイミングを手動で制御できるテスト用リポジトリ
 * owner/name が同じでも呼び出し順（インデックス）で区別し、[complete] を呼ぶまで [getRepository] は中断したままになる
 */
private class GatedFakeDetailRepository : RepoDetailRepository {
    private val gates = mutableListOf<CompletableDeferred<DataResult<RepoDetail>>>()

    override suspend fun getRepository(owner: String, name: String): DataResult<RepoDetail> {
        val gate = CompletableDeferred<DataResult<RepoDetail>>()
        gates.add(gate)
        return gate.await()
    }

    fun complete(index: Int, result: DataResult<RepoDetail>) {
        gates[index].complete(result)
    }
}
