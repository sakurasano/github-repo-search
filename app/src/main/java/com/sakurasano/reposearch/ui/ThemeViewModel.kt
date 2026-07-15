package com.sakurasano.reposearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakurasano.reposearch.data.ThemeRepository
import com.sakurasano.reposearch.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: ThemeRepository,
) : ViewModel() {

    val uiState: StateFlow<ThemeUiState> = repository.themeMode
        .map { themeMode -> ThemeUiState.Success(themeMode) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeUiState.Loading,
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }
}
