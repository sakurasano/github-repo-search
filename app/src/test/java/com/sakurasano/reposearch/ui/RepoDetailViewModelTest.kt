package com.sakurasano.reposearch.ui

import androidx.lifecycle.SavedStateHandle
import com.sakurasano.reposearch.MainDispatcherRule
import com.sakurasano.reposearch.data.FakeRepoDetailRepository
import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoDetail
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
