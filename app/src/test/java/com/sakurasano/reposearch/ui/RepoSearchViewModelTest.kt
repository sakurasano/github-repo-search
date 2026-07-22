package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.MainDispatcherRule
import com.sakurasano.reposearch.data.FakeFavoriteRepository
import com.sakurasano.reposearch.data.FakeRepoSearchRepository
import com.sakurasano.reposearch.data.FakeSearchHistoryRepository
import com.sakurasano.reposearch.data.RepoSearchRepository
import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSearchPage
import com.sakurasano.reposearch.model.RepoSummary
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
class RepoSearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `検索が成功するとSuccessになる`() = runTest {
        val repos = listOf(sampleRepo())
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess(repos)),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )

        viewModel.search("compose")

        assertEquals(RepoSearchUiState.Success(repos, LoadMoreState.End), viewModel.uiState.value)
    }

    @Test
    fun `検索結果が0件だとEmptyになる`() = runTest {
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess()),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )

        viewModel.search("no-such-repository")

        assertEquals(RepoSearchUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `検索が失敗するとErrorになる`() = runTest {
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(DataResult.Failure(AppError.Network)),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )

        viewModel.search("compose")

        assertEquals(RepoSearchUiState.Error(AppError.Network), viewModel.uiState.value)
    }

    @Test
    fun `空白のクエリでは検索せずIdleのままになる`() = runTest {
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess()),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )

        viewModel.search("   ")

        assertEquals(RepoSearchUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `連続検索では後の検索が優先され前の検索結果で上書きされない`() = runTest {
        val repo = GatedFakeRepository()
        val viewModel = RepoSearchViewModel(repo, FakeSearchHistoryRepository(), FakeFavoriteRepository())

        val oldRepos = listOf(sampleRepo("old"))
        val newRepos = listOf(sampleRepo("new"))

        viewModel.search("old") // 1回目: 応答待ちで中断
        viewModel.search("new") // 2回目: 1回目をキャンセルして開始し、応答待ちで中断

        // あえて新→旧の順で応答を返す（遅い前回=oldが後から返るケース）
        repo.complete("new", 1, searchSuccess(newRepos))
        repo.complete("old", 1, searchSuccess(oldRepos))

        // oldはキャンセル済みなので状態を上書きせず、newの結果が残る
        assertEquals(RepoSearchUiState.Success(newRepos, LoadMoreState.End), viewModel.uiState.value)
    }

    @Test
    fun `検索を実行するとそのクエリが履歴に記録される`() = runTest {
        val history = FakeSearchHistoryRepository()
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess(listOf(sampleRepo()))),
            history,
            FakeFavoriteRepository(),
        )

        viewModel.search("compose")
        advanceUntilIdle()

        assertTrue(history.history.value.contains("compose"))
    }

    @Test
    fun `結果0件でも実行したクエリは履歴に記録される`() = runTest {
        val history = FakeSearchHistoryRepository()
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess()),
            history,
            FakeFavoriteRepository(),
        )

        viewModel.search("no-such-repository")
        advanceUntilIdle()

        assertTrue(history.history.value.contains("no-such-repository"))
    }

    @Test
    fun `検索が失敗しても実行したクエリは履歴に記録される`() = runTest {
        val history = FakeSearchHistoryRepository()
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(DataResult.Failure(AppError.Network)),
            history,
            FakeFavoriteRepository(),
        )

        viewModel.search("compose")
        advanceUntilIdle()

        assertTrue(history.history.value.contains("compose"))
    }

    @Test
    fun `履歴を削除すると当該クエリが履歴から消える`() = runTest {
        val history = FakeSearchHistoryRepository(listOf("compose", "hilt"))
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess()),
            history,
            FakeFavoriteRepository(),
        )

        viewModel.removeHistory("compose")
        advanceUntilIdle()

        assertFalse(history.history.value.contains("compose"))
        assertTrue(history.history.value.contains("hilt"))
    }

    @Test
    fun `履歴を全消去すると履歴が空になる`() = runTest {
        val history = FakeSearchHistoryRepository(listOf("compose", "hilt"))
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess()),
            history,
            FakeFavoriteRepository(),
        )

        viewModel.clearHistory()
        advanceUntilIdle()

        assertTrue(history.history.value.isEmpty())
    }

    @Test
    fun `連続検索で前の検索が中断されても両方のクエリが記録される`() = runTest {
        val history = FakeSearchHistoryRepository()
        val repo = GatedFakeRepository()
        val viewModel = RepoSearchViewModel(repo, history, FakeFavoriteRepository())

        viewModel.search("kotlin") // 応答待ちで中断（searchJob進行中）
        viewModel.search("compose") // searchJobをキャンセルして再開
        advanceUntilIdle()

        // 記録はsearchJobと別に起動するため、キャンセルされた前回検索のクエリも残る
        assertTrue(history.history.value.contains("kotlin"))
        assertTrue(history.history.value.contains("compose"))
    }

    @Test
    fun `既にお気に入りのIDがfavoriteIdsに反映される`() = runTest {
        val repo = sampleRepo()
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess()),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(listOf(repo)),
        )
        backgroundScope.launch { viewModel.favoriteIds.collect {} }
        advanceUntilIdle()

        assertTrue(repo.id in viewModel.favoriteIds.value)
    }

    @Test
    fun `未登録のリポジトリをtoggleするとお気に入りに追加される`() = runTest {
        val repo = sampleRepo()
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess()),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )
        backgroundScope.launch { viewModel.favoriteIds.collect {} }
        advanceUntilIdle()

        viewModel.toggleFavorite(repo)
        advanceUntilIdle()

        assertTrue(repo.id in viewModel.favoriteIds.value)
    }

    @Test
    fun `登録済みのリポジトリをtoggleするとお気に入りから削除される`() = runTest {
        val repo = sampleRepo()
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess()),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(listOf(repo)),
        )
        backgroundScope.launch { viewModel.favoriteIds.collect {} }
        advanceUntilIdle()

        viewModel.toggleFavorite(repo)
        advanceUntilIdle()

        assertFalse(repo.id in viewModel.favoriteIds.value)
    }

    @Test
    fun `お気に入りの保存に失敗すると保存失敗イベントが流れる`() = runTest {
        val repo = sampleRepo()
        val favorites = FakeFavoriteRepository().also { it.failWrites = true }
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess()),
            FakeSearchHistoryRepository(),
            favorites,
        )
        val events = mutableListOf<Unit>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.writeFailed.collect { events.add(it) }
        }

        viewModel.toggleFavorite(repo)
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, events.size)
    }

    @Test
    fun `続きがある検索結果では追加読み込み待機状態になる`() = runTest {
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess(listOf(sampleRepo()), hasMore = true)),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )

        viewModel.search("compose")

        assertEquals(LoadMoreState.Idle, (viewModel.uiState.value as RepoSearchUiState.Success).loadMore)
    }

    @Test
    fun `追加読み込みすると次のページが末尾に追記される`() = runTest {
        val page1 = listOf(sampleRepo("a"))
        val page2 = listOf(sampleRepo("b"))
        val viewModel = RepoSearchViewModel(
            PagedFakeRepository(
                mapOf(
                    1 to searchSuccess(page1, hasMore = true),
                    2 to searchSuccess(page2, hasMore = false),
                ),
            ),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )

        viewModel.search("q")
        viewModel.loadMore()

        assertEquals(RepoSearchUiState.Success(page1 + page2, LoadMoreState.End), viewModel.uiState.value)
    }

    @Test
    fun `追加読み込みが失敗するとリストを保ったままエラー状態になる`() = runTest {
        val page1 = listOf(sampleRepo("a"))
        val viewModel = RepoSearchViewModel(
            PagedFakeRepository(
                mapOf(
                    1 to searchSuccess(page1, hasMore = true),
                    2 to DataResult.Failure(AppError.Network),
                ),
            ),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )

        viewModel.search("q")
        viewModel.loadMore()

        assertEquals(
            RepoSearchUiState.Success(page1, LoadMoreState.Error(AppError.Network)),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `追加読み込みの失敗後に再実行すると同じページを取り直す`() = runTest {
        val page1 = listOf(sampleRepo("a"))
        val page2 = listOf(sampleRepo("b"))
        val repo = RetryFakeRepository(
            page1 = searchSuccess(page1, hasMore = true),
            page2Attempts = listOf(DataResult.Failure(AppError.Network), searchSuccess(page2, hasMore = false)),
        )
        val viewModel = RepoSearchViewModel(repo, FakeSearchHistoryRepository(), FakeFavoriteRepository())

        viewModel.search("q")
        viewModel.loadMore() // 2ページ目に失敗
        viewModel.loadMore() // リトライ

        assertEquals(RepoSearchUiState.Success(page1 + page2, LoadMoreState.End), viewModel.uiState.value)
        // 3ページ目に進まず、失敗した2ページ目を取り直している
        assertEquals(listOf(1, 2, 2), repo.requestedPages)
    }

    @Test
    fun `追加読み込み中に再度実行しても二重に読み込まない`() = runTest {
        val repo = GatedFakeRepository()
        val viewModel = RepoSearchViewModel(repo, FakeSearchHistoryRepository(), FakeFavoriteRepository())
        val page1 = listOf(sampleRepo("a"))
        val page2 = listOf(sampleRepo("b"))

        viewModel.search("q")
        repo.complete("q", 1, searchSuccess(page1, hasMore = true))

        viewModel.loadMore() // 2ページ目を要求して応答待ち
        viewModel.loadMore() // 応答待ちの間の重複要求は無視される
        repo.complete("q", 2, searchSuccess(page2, hasMore = false))

        assertEquals(RepoSearchUiState.Success(page1 + page2, LoadMoreState.End), viewModel.uiState.value)
    }

    @Test
    fun `追加読み込み中に新しい検索をすると古い追加結果は反映されない`() = runTest {
        val repo = GatedFakeRepository()
        val viewModel = RepoSearchViewModel(repo, FakeSearchHistoryRepository(), FakeFavoriteRepository())
        val oldPage1 = listOf(sampleRepo("old1"))
        val oldPage2 = listOf(sampleRepo("old2"))
        val newPage1 = listOf(sampleRepo("new1"))

        viewModel.search("old")
        repo.complete("old", 1, searchSuccess(oldPage1, hasMore = true))
        viewModel.loadMore() // (old, 2) を要求して応答待ち

        viewModel.search("new") // 進行中の追加読み込みを打ち切る
        repo.complete("old", 2, searchSuccess(oldPage2, hasMore = true)) // 遅れて返る古い追加結果
        repo.complete("new", 1, searchSuccess(newPage1, hasMore = false))

        assertEquals(RepoSearchUiState.Success(newPage1, LoadMoreState.End), viewModel.uiState.value)
    }

    @Test
    fun `終端では追加読み込みを実行しても状態が変わらない`() = runTest {
        val viewModel = RepoSearchViewModel(
            FakeRepoSearchRepository(searchSuccess(listOf(sampleRepo()), hasMore = false)),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )

        viewModel.search("q")
        val before = viewModel.uiState.value
        viewModel.loadMore()

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun `ページ間で重複するIDは追記時に除外される`() = runTest {
        val shared = sampleRepo("shared")
        val page1 = listOf(shared, sampleRepo("a"))
        val page2 = listOf(shared, sampleRepo("b")) // sharedがページ1と重複
        val viewModel = RepoSearchViewModel(
            PagedFakeRepository(
                mapOf(
                    1 to searchSuccess(page1, hasMore = true),
                    2 to searchSuccess(page2, hasMore = false),
                ),
            ),
            FakeSearchHistoryRepository(),
            FakeFavoriteRepository(),
        )

        viewModel.search("q")
        viewModel.loadMore()

        val result = (viewModel.uiState.value as RepoSearchUiState.Success).repos
        assertEquals(listOf(shared, sampleRepo("a"), sampleRepo("b")), result)
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

    private fun searchSuccess(repos: List<RepoSummary> = emptyList(), hasMore: Boolean = false) =
        DataResult.Success(RepoSearchPage(repos, hasMore))
}

/**
 * クエリごとに応答タイミングを手動で制御できるテスト用リポジトリ
 * [complete] を呼ぶまで [searchRepositories] は中断したままになる
 */
private class GatedFakeRepository : RepoSearchRepository {
    private val gates = mutableMapOf<Pair<String, Int>, CompletableDeferred<DataResult<RepoSearchPage>>>()

    override suspend fun searchRepositories(query: String, page: Int): DataResult<RepoSearchPage> =
        gates.getOrPut(query to page) { CompletableDeferred() }.await()

    fun complete(query: String, page: Int, result: DataResult<RepoSearchPage>) {
        gates.getOrPut(query to page) { CompletableDeferred() }.complete(result)
    }
}

/**
 * ページ番号ごとに固定結果を返すテスト用リポジトリ
 */
private class PagedFakeRepository(
    private val pages: Map<Int, DataResult<RepoSearchPage>>,
) : RepoSearchRepository {
    override suspend fun searchRepositories(query: String, page: Int): DataResult<RepoSearchPage> =
        pages.getValue(page)
}

/**
 * 2ページ目だけ呼ばれるたびに結果を切り替えるテスト用リポジトリ。
 * [requestedPages] で要求されたページ番号を検証できる（リトライで同じページを取り直すか等）
 */
private class RetryFakeRepository(
    private val page1: DataResult<RepoSearchPage>,
    private val page2Attempts: List<DataResult<RepoSearchPage>>,
) : RepoSearchRepository {
    val requestedPages = mutableListOf<Int>()
    private var attempt = 0

    override suspend fun searchRepositories(query: String, page: Int): DataResult<RepoSearchPage> {
        requestedPages.add(page)
        return if (page == 1) page1 else page2Attempts[attempt++]
    }
}
