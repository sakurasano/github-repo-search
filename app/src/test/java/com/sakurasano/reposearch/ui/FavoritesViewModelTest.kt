package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.MainDispatcherRule
import com.sakurasano.reposearch.data.FakeFavoriteRepository
import com.sakurasano.reposearch.model.RepoSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `お気に入りが0件だとEmptyになる`() = runTest {
        val viewModel = FavoritesViewModel(FakeFavoriteRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(FavoritesUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `お気に入りがあるとSuccessになる`() = runTest {
        val repos = listOf(sampleRepo())
        val viewModel = FavoritesViewModel(FakeFavoriteRepository(repos))
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(FavoritesUiState.Success(repos), viewModel.uiState.value)
    }

    @Test
    fun `読み取りに失敗するとErrorになる`() = runTest {
        val repository = FakeFavoriteRepository().apply { failReads = true }
        val viewModel = FavoritesViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(FavoritesUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `removeFavoriteで一覧から消えEmptyになる`() = runTest {
        val repo = sampleRepo()
        val repository = FakeFavoriteRepository(listOf(repo))
        val viewModel = FavoritesViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.removeFavorite(repo.id)
        advanceUntilIdle()

        assertEquals(FavoritesUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `removeFavoriteが失敗すると保存失敗イベントが流れる`() = runTest {
        val repo = sampleRepo()
        val repository = FakeFavoriteRepository(listOf(repo)).apply { failWrites = true }
        val viewModel = FavoritesViewModel(repository)
        val events = mutableListOf<Unit>()
        // Unconfinedで即座に購読を開始させ、失敗イベントを取りこぼさないようにする
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.saveFailed.collect { events.add(it) }
        }

        viewModel.removeFavorite(repo.id)
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, events.size)
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
