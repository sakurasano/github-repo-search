package com.sakurasano.reposearch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakurasano.reposearch.R
import com.sakurasano.reposearch.model.RepoSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onRepoClick: (RepoSummary) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailedMessage = stringResource(R.string.favorite_save_failed)

    LaunchedEffect(Unit) {
        viewModel.saveFailed.collect { snackbarHostState.showSnackbar(saveFailedMessage) }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            FavoritesUiState.Loading -> LoadingIndicator(Modifier.padding(innerPadding))

            FavoritesUiState.Empty -> StatusMessage(
                icon = ImageVector.vectorResource(R.drawable.ic_star_border),
                message = stringResource(R.string.favorites_empty),
                modifier = Modifier.padding(innerPadding),
            )

            FavoritesUiState.Error -> StatusMessage(
                icon = ImageVector.vectorResource(R.drawable.ic_error_outline),
                message = stringResource(R.string.favorites_error),
                modifier = Modifier.padding(innerPadding),
                onRetry = viewModel::retry,
                retryLabel = stringResource(R.string.favorites_retry),
            )

            is FavoritesUiState.Success -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = state.repos,
                    key = { it.id },
                ) { repo ->
                    RepoCard(
                        repo = repo,
                        onClick = { onRepoClick(repo) },
                        isFavorite = true,
                        onToggleFavorite = { viewModel.removeFavorite(repo.id) },
                    )
                }
            }
        }
    }
}
