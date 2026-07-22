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

    // 確定した検索キーワード。一覧のスクロール位置はこれを識別子として保持/リセットされる
    private val _searchedQuery = MutableStateFlow("")
    val searchedQuery: StateFlow<String> = _searchedQuery.asStateFlow()

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null
    private var currentPage: Int = 0

    fun search(query: String) {
        if (query.isBlank()) return
        // 履歴の記録はsearchJobと別に起動する。連続検索でsearchJobをcancelしても記録が巻き添えにならないようにするため
        viewModelScope.launch { searchHistoryRepository.record(query) }
        // 進行中の検索と追加読み込みをともに打ち切り、遅い前回結果が新しい結果を上書きするのを防ぐ
        searchJob?.cancel()
        loadMoreJob?.cancel()
        _searchedQuery.value = query
        searchJob = viewModelScope.launch {
            _uiState.value = RepoSearchUiState.Loading
            val result = repoSearchRepository.searchRepositories(query, page = 1)
            _uiState.value = when (result) {
                is DataResult.Success -> {
                    currentPage = 1
                    val page = result.data
                    if (page.items.isEmpty()) {
                        RepoSearchUiState.Empty
                    } else {
                        RepoSearchUiState.Success(page.items, LoadMoreState.from(page.hasMore))
                    }
                }

                is DataResult.Failure -> RepoSearchUiState.Error(result.error)
            }
        }
    }

    fun loadMore() {
        val current = _uiState.value
        // 失敗状態からのリトライは許可する
        if (current !is RepoSearchUiState.Success) return
        if (current.loadMoreState == LoadMoreState.Loading || current.loadMoreState == LoadMoreState.End) return
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            _uiState.value = current.copy(loadMoreState = LoadMoreState.Loading)
            val result = repoSearchRepository.searchRepositories(_searchedQuery.value, page = currentPage + 1)
            _uiState.value = when (result) {
                is DataResult.Success -> {
                    currentPage += 1
                    val page = result.data
                    // ページ間で重複するIDを取り除く（LazyColumnのkey重複によるクラッシュを防ぐ）
                    val merged = (current.repos + page.items).distinctBy { it.id }
                    RepoSearchUiState.Success(merged, LoadMoreState.from(page.hasMore))
                }

                is DataResult.Failure -> current.copy(loadMoreState = LoadMoreState.Error(result.error))
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
