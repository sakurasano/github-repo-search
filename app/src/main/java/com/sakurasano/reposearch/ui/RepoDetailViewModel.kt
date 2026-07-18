package com.sakurasano.reposearch.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakurasano.reposearch.data.FavoriteRepository
import com.sakurasano.reposearch.data.RepoDetailRepository
import com.sakurasano.reposearch.data.toSummary
import com.sakurasano.reposearch.model.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RepoDetailViewModel @Inject constructor(
    private val repository: RepoDetailRepository,
    private val favoriteRepository: FavoriteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // savedStateHandle.toRoute()だとBundle依存でJVM単体テストで動かないので、キーで直接読んでいる（キー名はRepoDetailRouteのプロパティ名と一致）
    private val owner: String = checkNotNull(savedStateHandle["owner"])
    private val name: String = checkNotNull(savedStateHandle["name"])

    private val _uiState = MutableStateFlow<RepoDetailUiState>(RepoDetailUiState.Loading)
    val uiState: StateFlow<RepoDetailUiState> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = uiState
        .flatMapLatest { state ->
            if (state is RepoDetailUiState.Success) {
                favoriteRepository.observeIsFavorite(state.repo.id)
            } else {
                flowOf(false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val writeNotifier = FavoriteWriteNotifier()
    val saveFailed = writeNotifier.saveFailed

    private var fetchJob: Job? = null

    init {
        fetch()
    }

    fun toggleFavorite() {
        val state = uiState.value
        if (state !is RepoDetailUiState.Success) return
        val repo = state.repo
        viewModelScope.launch {
            val result = if (isFavorite.value) {
                favoriteRepository.remove(repo.id)
            } else {
                favoriteRepository.add(repo.toSummary())
            }
            writeNotifier.notifyIfFailure(result)
        }
    }

    fun retry() = fetch()

    private fun fetch() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = RepoDetailUiState.Loading
            _uiState.value = when (val result = repository.getRepository(owner, name)) {
                is DataResult.Success -> RepoDetailUiState.Success(result.data)
                is DataResult.Failure -> RepoDetailUiState.Error(result.error)
            }
        }
    }
}
