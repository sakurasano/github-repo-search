package com.sakurasano.reposearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakurasano.reposearch.data.FavoriteRepository
import com.sakurasano.reposearch.data.RepoSearchRepository
import com.sakurasano.reposearch.data.SearchHistoryRepository
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoSearchViewModel @Inject constructor(
    private val repoSearchRepository: RepoSearchRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RepoSearchUiState>(RepoSearchUiState.Idle)
    val uiState: StateFlow<RepoSearchUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<String>> = searchHistoryRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteIds: StateFlow<Set<Long>> = favoriteRepository.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val writeNotifier = FavoriteWriteNotifier()
    val writeFailed = writeNotifier.writeFailed

    private var searchJob: Job? = null

    fun search(query: String) {
        if (query.isBlank()) return
        // 履歴の記録はsearchJobと別に起動する。連続検索でsearchJobをcancelしても記録が巻き添えにならないようにするため
        viewModelScope.launch { searchHistoryRepository.record(query) }
        searchJob?.cancel() // 進行中の前回検索を打ち切り、遅い前回結果が新しい結果を上書きするのを防ぐ
        searchJob = viewModelScope.launch {
            _uiState.value = RepoSearchUiState.Loading
            _uiState.value = when (val result = repoSearchRepository.searchRepositories(query)) {
                is DataResult.Success ->
                    if (result.data.isEmpty()) {
                        RepoSearchUiState.Empty
                    } else {
                        RepoSearchUiState.Success(result.data)
                    }

                is DataResult.Failure -> RepoSearchUiState.Error(result.error)
            }
        }
    }

    fun toggleFavorite(repo: RepoSummary) {
        viewModelScope.launch {
            val result = favoriteRepository.toggle(repo)
            writeNotifier.notifyIfFailure(result)
        }
    }

    fun removeHistory(query: String) {
        viewModelScope.launch { searchHistoryRepository.remove(query) }
    }

    fun clearHistory() {
        viewModelScope.launch { searchHistoryRepository.clear() }
    }
}
