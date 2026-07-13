package com.sakurasano.reposearch.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakurasano.reposearch.data.RepoDetailRepository
import com.sakurasano.reposearch.model.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoDetailViewModel @Inject constructor(
    private val repository: RepoDetailRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // savedStateHandle.toRoute()だとBundle依存でJVM単体テストで動かないので、キーで直接読んでいる（キー名はRepoDetailRouteのプロパティ名と一致）
    private val owner: String = checkNotNull(savedStateHandle["owner"])
    private val name: String = checkNotNull(savedStateHandle["name"])

    private val _uiState = MutableStateFlow<RepoDetailUiState>(RepoDetailUiState.Loading)
    val uiState: StateFlow<RepoDetailUiState> = _uiState.asStateFlow()

    init {
        fetch()
    }

    fun retry() = fetch()

    private fun fetch() {
        viewModelScope.launch {
            _uiState.value = RepoDetailUiState.Loading
            _uiState.value = when (val result = repository.getRepository(owner, name)) {
                is DataResult.Success -> RepoDetailUiState.Success(result.data)
                is DataResult.Failure -> RepoDetailUiState.Error(result.error)
            }
        }
    }
}
