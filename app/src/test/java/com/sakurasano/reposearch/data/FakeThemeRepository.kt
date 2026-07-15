package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeThemeRepository(initial: ThemeMode = ThemeMode.SYSTEM) : ThemeRepository {
    private val state = MutableStateFlow(initial)
    override val themeMode: Flow<ThemeMode> = state

    override suspend fun setThemeMode(mode: ThemeMode) {
        state.value = mode
    }
}
