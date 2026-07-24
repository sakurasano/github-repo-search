package com.sakurasano.reposearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakurasano.reposearch.data.FavoriteRepository
import com.sakurasano.reposearch.data.RepoSearchRepository
import com.sakurasano.reposearch.data.SearchHistoryRepository
import com.sakurasano.reposearch.data.SearchSortRepository
import com.sakurasano.reposearch.model.DataResult
import com.sakurasano.reposearch.model.RepoSummary
import com.sakurasano.reposearch.model.SearchSort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoSearchViewModel @Inject constructor(
    private val repoSearchRepository: RepoSearchRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val searchSortRepository: SearchSortRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RepoSearchUiState>(RepoSearchUiState.Idle)
    val uiState: StateFlow<RepoSearchUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<String>> = searchHistoryRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteIds: StateFlow<Set<Long>> = favoriteRepository.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val writeNotifier = FavoriteWriteNotifier()
    val writeFailed = writeNotifier.writeFailed

    // 確定した検索条件（キーワード＋並び順）。一覧のスクロール位置はこれを識別子として保持/リセットされる
    private val _searchParams = MutableStateFlow(SearchParams(query = "", sort = SearchSort.BEST_MATCH))
    val searchParams: StateFlow<SearchParams> = _searchParams.asStateFlow()

    // ユーザーが検索/並び順選択を行ったか。永続値の初期反映でユーザー操作を上書きしないための番兵
    private var sortInitialized = false

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null
    private var currentPage: Int = 0

    init {
        viewModelScope.launch {
            val saved = searchSortRepository.sortOption.first()
            // 先にユーザーが検索/選択していたら、遅れて読めた古い値でそれを上書きしない
            if (!sortInitialized) {
                sortInitialized = true
                _searchParams.value = _searchParams.value.copy(sort = saved)
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        sortInitialized = true
        // 履歴の記録はsearchJobと別に起動する。連続検索でsearchJobをcancelしても記録が巻き添えにならないようにするため
        viewModelScope.launch { searchHistoryRepository.record(query) }
        runSearch(query, _searchParams.value.sort)
    }

    // 失敗した検索を取り直す。入力欄のその後の編集に左右されないよう、確定済みの検索条件を使う
    fun retry() {
        val params = _searchParams.value
        if (params.query.isBlank()) return
        runSearch(params.query, params.sort)
    }

    fun selectSort(sort: SearchSort) {
        sortInitialized = true
        if (sort == _searchParams.value.sort) return
        viewModelScope.launch { searchSortRepository.setSortOption(sort) }
        val query = _searchParams.value.query
        if (query.isNotBlank()) {
            runSearch(query, sort)
        } else {
            _searchParams.value = _searchParams.value.copy(sort = sort)
        }
    }

    // 1ページ目から新規に取り直す。検索・再試行・並び順変更の共通経路
    private fun runSearch(query: String, sort: SearchSort) {
        // 進行中の検索と追加読み込みをともに打ち切り、遅い前回結果が新しい結果を上書きするのを防ぐ
        searchJob?.cancel()
        loadMoreJob?.cancel()
        _searchParams.value = SearchParams(query, sort)
        searchJob = viewModelScope.launch {
            _uiState.value = RepoSearchUiState.Loading
            val result = repoSearchRepository.searchRepositories(query, page = 1, sort = sort)
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
        val params = _searchParams.value
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            _uiState.value = current.copy(loadMoreState = LoadMoreState.Loading)
            val result = repoSearchRepository.searchRepositories(
                params.query,
                page = currentPage + 1,
                sort = params.sort,
            )
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
