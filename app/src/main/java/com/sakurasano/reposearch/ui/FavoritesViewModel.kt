package com.sakurasano.reposearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakurasano.reposearch.data.FavoriteRepository
import com.sakurasano.reposearch.model.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
            favoriteRepository.favorites.map { result ->
                when (result) {
                    is DataResult.Success ->
                        if (result.data.isEmpty()) {
                            FavoritesUiState.Empty
                        } else {
                            FavoritesUiState.Success(result.data)
                        }

                    is DataResult.Failure -> FavoritesUiState.Error
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState.Loading)

    private val writeNotifier = FavoriteWriteNotifier()
    val writeFailed = writeNotifier.writeFailed

    fun retry() {
        retryTrigger.value++
    }

    fun removeFavorite(id: Long) {
        viewModelScope.launch { writeNotifier.notifyIfFailure(favoriteRepository.remove(id)) }
    }
}
