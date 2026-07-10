package com.sakurasano.reposearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.sakurasano.reposearch.ui.RepoSearchScreen
import com.sakurasano.reposearch.ui.theme.GitHubRepoSearchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GitHubRepoSearchTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RepoSearchScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
