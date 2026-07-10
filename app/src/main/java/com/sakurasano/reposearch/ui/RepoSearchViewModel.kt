package com.sakurasano.reposearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakurasano.reposearch.data.RepoSearchRepository
import com.sakurasano.reposearch.model.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoSearchViewModel @Inject constructor(
    private val repository: RepoSearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RepoSearchUiState>(RepoSearchUiState.Idle)
    val uiState: StateFlow<RepoSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        if (query.isBlank()) return
        searchJob?.cancel() // 進行中の前回検索を打ち切り、遅い前回結果が新しい結果を上書きするのを防ぐ
        searchJob = viewModelScope.launch {
            _uiState.value = RepoSearchUiState.Loading
            _uiState.value = when (val result = repository.searchRepositories(query)) {
                is DataResult.Success ->
                    if (result.data.isEmpty()) RepoSearchUiState.Empty
                    else RepoSearchUiState.Success(result.data)

                is DataResult.Failure -> RepoSearchUiState.Error(result.error)
            }
        }
    }
}
