package com.sakurasano.reposearch

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sakurasano.reposearch.model.ThemeMode
import com.sakurasano.reposearch.ui.ThemeUiState
import com.sakurasano.reposearch.ui.ThemeViewModel
import com.sakurasano.reposearch.ui.navigation.AppNavHost
import com.sakurasano.reposearch.ui.theme.GitHubRepoSearchTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: ThemeUiState by mutableStateOf(ThemeUiState.Loading)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeViewModel.uiState.collect { uiState = it }
            }
        }
        // テーマが確定するまではスプラッシュを保持し、起動時のテーマのちらつきを防ぐ
        splashScreen.setKeepOnScreenCondition { uiState is ThemeUiState.Loading }

        setContent {
            val darkTheme = shouldUseDarkTheme(uiState)

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                )
                onDispose {}
            }

            GitHubRepoSearchTheme(darkTheme = darkTheme) {
                AppNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun shouldUseDarkTheme(uiState: ThemeUiState): Boolean = when (uiState) {
    ThemeUiState.Loading -> isSystemInDarkTheme()

    is ThemeUiState.Success -> when (uiState.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}
