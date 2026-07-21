package com.sakurasano.reposearch.ui

import androidx.lifecycle.SavedStateHandle
import com.sakurasano.reposearch.MainDispatcherRule
import com.sakurasano.reposearch.data.FakeFavoriteRepository
import com.sakurasano.reposearch.data.FakeRepoDetailRepository
import com.sakurasano.reposearch.data.RepoDetailRepository
import com.sakurasano.reposearch.data.toSummary
import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoDetail
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
            FakeFavoriteRepository(),
            savedStateHandle,
        )

        assertEquals(RepoDetailUiState.Success(repo), viewModel.uiState.value)
    }

    @Test
    fun `詳細取得が失敗するとErrorになる`() = runTest {
        val viewModel = RepoDetailViewModel(
            FakeRepoDetailRepository(DataResult.Failure(AppError.Network)),
            FakeFavoriteRepository(),
            savedStateHandle,
        )

        assertEquals(RepoDetailUiState.Error(AppError.Network), viewModel.uiState.value)
    }

    @Test
    fun `再試行で再度取得しSuccessになる`() = runTest {
        val repo = sampleDetail()
        val repository = FakeRepoDetailRepository(DataResult.Failure(AppError.Network))
        val viewModel = RepoDetailViewModel(repository, FakeFavoriteRepository(), savedStateHandle)

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
        val viewModel = RepoDetailViewModel(repo, FakeFavoriteRepository(), savedStateHandle)

        val oldDetail = sampleDetail("old")
        val newDetail = sampleDetail("new")

        viewModel.retry() // 1回目をキャンセルして2回目を開始し、応答待ちで中断

        // あえて 新(1)→旧(0) の順で応答を返す（遅い前回=oldが後から返るケース）
        repo.complete(1, DataResult.Success(newDetail))
        repo.complete(0, DataResult.Success(oldDetail))

        // 0はキャンセル済みなので状態を上書きせず、1の結果が残る
        assertEquals(RepoDetailUiState.Success(newDetail), viewModel.uiState.value)
    }

    @Test
    fun `既にお気に入りならisFavoriteがtrueになる`() = runTest {
        val detail = sampleDetail()
        val viewModel = RepoDetailViewModel(
            FakeRepoDetailRepository(DataResult.Success(detail)),
            FakeFavoriteRepository(listOf(detail.toSummary())),
            savedStateHandle,
        )
        backgroundScope.launch { viewModel.isFavorite.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite.value)
    }

    @Test
    fun `未登録のリポジトリをtoggleするとisFavoriteがtrueになる`() = runTest {
        val detail = sampleDetail()
        val viewModel = RepoDetailViewModel(
            FakeRepoDetailRepository(DataResult.Success(detail)),
            FakeFavoriteRepository(),
            savedStateHandle,
        )
        backgroundScope.launch { viewModel.isFavorite.collect {} }
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite.value)
    }

    @Test
    fun `登録済みをtoggleするとisFavoriteがfalseになる`() = runTest {
        val detail = sampleDetail()
        val viewModel = RepoDetailViewModel(
            FakeRepoDetailRepository(DataResult.Success(detail)),
            FakeFavoriteRepository(listOf(detail.toSummary())),
            savedStateHandle,
        )
        backgroundScope.launch { viewModel.isFavorite.collect {} }
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        assertFalse(viewModel.isFavorite.value)
    }

    @Test
    fun `お気に入りの保存に失敗すると保存失敗イベントが流れる`() = runTest {
        val detail = sampleDetail()
        val favorites = FakeFavoriteRepository().also { it.failWrites = true }
        val viewModel = RepoDetailViewModel(
            FakeRepoDetailRepository(DataResult.Success(detail)),
            favorites,
            savedStateHandle,
        )
        val events = mutableListOf<Unit>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.writeFailed.collect { events.add(it) }
        }

        viewModel.toggleFavorite()
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, events.size)
    }

    private fun sampleDetail(name: String = "nowinandroid") = RepoDetail(
        id = name.hashCode().toLong(),
        name = name,
        fullName = "android/$name",
        htmlUrl = "https://github.com/android/$name",
        description = "説明",
        ownerName = "android",
        ownerAvatarUrl = "https://example.com/avatar.png",
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
