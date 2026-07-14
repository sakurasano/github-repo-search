package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.MainDispatcherRule
import com.sakurasano.reposearch.data.FakeThemeRepository
import com.sakurasano.reposearch.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `初期状態はLoadingになる`() {
        val viewModel = ThemeViewModel(FakeThemeRepository())

        assertEquals(ThemeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `保存済みのテーマが読み込まれるとSuccessになる`() = runTest {
        val viewModel = ThemeViewModel(FakeThemeRepository(ThemeMode.SYSTEM))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        assertEquals(ThemeUiState.Success(ThemeMode.SYSTEM), viewModel.uiState.value)
    }

    @Test
    fun `テーマを変更すると選択したテーマが反映される`() = runTest {
        val viewModel = ThemeViewModel(FakeThemeRepository(ThemeMode.SYSTEM))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeUiState.Success(ThemeMode.DARK), viewModel.uiState.value)
    }
}
