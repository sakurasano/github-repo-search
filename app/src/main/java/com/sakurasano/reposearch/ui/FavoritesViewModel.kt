package com.sakurasano.reposearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakurasano.reposearch.data.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<FavoritesUiState> = retryTrigger
        .flatMapLatest {
            favoriteRepository.favorites
                .map { repos ->
                    if (repos.isEmpty()) FavoritesUiState.Empty else FavoritesUiState.Success(repos)
                }
                .catch { emit(FavoritesUiState.Error) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState.Loading)

    private val _saveFailed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saveFailed: SharedFlow<Unit> = _saveFailed.asSharedFlow()

    fun retry() {
        retryTrigger.value++
    }

    fun removeFavorite(id: Long) {
        viewModelScope.launch {
            _saveFailed.runFavoriteWrite { favoriteRepository.remove(id) }
        }
    }
}
