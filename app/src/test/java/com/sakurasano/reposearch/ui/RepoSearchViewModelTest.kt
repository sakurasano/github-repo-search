package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.MainDispatcherRule
import com.sakurasano.reposearch.data.FakeRepoSearchRepository
import com.sakurasano.reposearch.data.FakeSearchHistoryRepository
import com.sakurasano.reposearch.data.RepoSearchRepository
import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSummary
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RepoSearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `検索が成功するとSuccessになる`() = runTest {
        val repos = listOf(sampleRepo())
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(DataResult.Success(repos)),
            FakeSearchHistoryRepository(),
        )

        viewModel.search("compose")

        assertEquals(RepoSearchUiState.Success(repos), viewModel.uiState.value)
    }

    @Test
    fun `検索結果が0件だとEmptyになる`() = runTest {
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(DataResult.Success(emptyList())),
            FakeSearchHistoryRepository(),
        )

        viewModel.search("no-such-repository")

        assertEquals(RepoSearchUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `検索が失敗するとErrorになる`() = runTest {
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(DataResult.Failure(AppError.Network)),
            FakeSearchHistoryRepository(),
        )

        viewModel.search("compose")

        assertEquals(RepoSearchUiState.Error(AppError.Network), viewModel.uiState.value)
    }

    @Test
    fun `空白のクエリでは検索せずIdleのままになる`() = runTest {
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(DataResult.Success(emptyList())),
            FakeSearchHistoryRepository(),
        )

        viewModel.search("   ")

        assertEquals(RepoSearchUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `連続検索では後の検索が優先され前の検索結果で上書きされない`() = runTest {
        val repo = GatedFakeRepository()
        val viewModel = RepoSearchViewModel(repo, FakeSearchHistoryRepository())

        val oldRepos = listOf(sampleRepo("old"))
        val newRepos = listOf(sampleRepo("new"))

        viewModel.search("old") // 1回目: 応答待ちで中断
        viewModel.search("new") // 2回目: 1回目をキャンセルして開始し、応答待ちで中断

        // あえて新→旧の順で応答を返す（遅い前回=oldが後から返るケース）
        repo.complete("new", DataResult.Success(newRepos))
        repo.complete("old", DataResult.Success(oldRepos))

        // oldはキャンセル済みなので状態を上書きせず、newの結果が残る
        assertEquals(RepoSearchUiState.Success(newRepos), viewModel.uiState.value)
    }

    @Test
    fun `検索を実行するとそのクエリが履歴に記録される`() = runTest {
        val history = FakeSearchHistoryRepository()
        val viewModel =
            RepoSearchViewModel(FakeRepoSearchRepository(DataResult.Success(listOf(sampleRepo()))), history)

        viewModel.search("compose")
        advanceUntilIdle()

        assertTrue(history.history.value.contains("compose"))
    }

    @Test
    fun `結果0件でも実行したクエリは履歴に記録される`() = runTest {
        val history = FakeSearchHistoryRepository()
        val viewModel =
            RepoSearchViewModel(FakeRepoSearchRepository(DataResult.Success(emptyList())), history)

        viewModel.search("no-such-repository")
        advanceUntilIdle()

        assertTrue(history.history.value.contains("no-such-repository"))
    }

    @Test
    fun `検索が失敗しても実行したクエリは履歴に記録される`() = runTest {
        val history = FakeSearchHistoryRepository()
        val viewModel =
            RepoSearchViewModel(FakeRepoSearchRepository(DataResult.Failure(AppError.Network)), history)

        viewModel.search("compose")
        advanceUntilIdle()

        assertTrue(history.history.value.contains("compose"))
    }

    @Test
    fun `履歴を削除すると当該クエリが履歴から消える`() = runTest {
        val history = FakeSearchHistoryRepository(listOf("compose", "hilt"))
        val viewModel =
            RepoSearchViewModel(FakeRepoSearchRepository(DataResult.Success(emptyList())), history)

        viewModel.removeHistory("compose")
        advanceUntilIdle()

        assertFalse(history.history.value.contains("compose"))
        assertTrue(history.history.value.contains("hilt"))
    }

    @Test
    fun `履歴を全消去すると履歴が空になる`() = runTest {
        val history = FakeSearchHistoryRepository(listOf("compose", "hilt"))
        val viewModel =
            RepoSearchViewModel(FakeRepoSearchRepository(DataResult.Success(emptyList())), history)

        viewModel.clearHistory()
        advanceUntilIdle()

        assertTrue(history.history.value.isEmpty())
    }

    private fun sampleRepo(name: String = "nowinandroid") = RepoSummary(
        id = name.hashCode().toLong(),
        name = name,
        fullName = "android/$name",
        description = "",
        ownerName = "android",
        ownerAvatarUrl = "https://example.com/avatar.png",
        starCount = 100,
        language = "Kotlin",
    )
}

/**
 * クエリごとに応答タイミングを手動で制御できるテスト用リポジトリ
 * [complete] を呼ぶまで [searchRepositories] は中断したままになる
 */
private class GatedFakeRepository : RepoSearchRepository {
    private val gates = mutableMapOf<String, CompletableDeferred<DataResult<List<RepoSummary>>>>()

    override suspend fun searchRepositories(query: String): DataResult<List<RepoSummary>> =
        gates.getOrPut(query) { CompletableDeferred() }.await()

    fun complete(query: String, result: DataResult<List<RepoSummary>>) {
        gates.getOrPut(query) { CompletableDeferred() }.complete(result)
    }
}
