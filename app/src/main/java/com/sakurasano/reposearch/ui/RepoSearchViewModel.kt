package com.sakurasano.reposearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakurasano.reposearch.data.RepoSearchRepository
import com.sakurasano.reposearch.model.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun search(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = RepoSearchUiState.Loading
            _uiState.value = when (val result = repository.searchRepositories(query)) {
                is DataResult.Success -> {
                    if (result.data.isEmpty()) RepoSearchUiState.Empty
                    else RepoSearchUiState.Success(result.data)
                }

                is DataResult.Failure -> {
                    RepoSearchUiState.Error(result.error)
                }
            }
        }
    }
}
