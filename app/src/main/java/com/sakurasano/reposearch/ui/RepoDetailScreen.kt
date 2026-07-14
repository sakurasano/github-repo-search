package com.sakurasano.reposearch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.sakurasano.reposearch.R
import com.sakurasano.reposearch.model.RepoDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RepoDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val title = when (val state = uiState) {
        is RepoDetailUiState.Success -> state.repo.name
        else -> stringResource(R.string.detail_title)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
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
            RepoDetailUiState.Loading -> CenterBox(Modifier.padding(innerPadding)) {
                CircularProgressIndicator()
            }

            is RepoDetailUiState.Error -> CenterBox(Modifier.padding(innerPadding)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(state.error.messageRes()))
                    Button(
                        onClick = viewModel::retry,
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Text(text = stringResource(R.string.detail_retry))
                    }
                }
            }

            is RepoDetailUiState.Success -> RepoDetailContent(
                repo = state.repo,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RepoDetailContent(repo: RepoDetail, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = repo.ownerAvatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            )
            Text(text = repo.ownerName, style = MaterialTheme.typography.bodyMedium)
        }
        Text(text = repo.fullName, style = MaterialTheme.typography.titleLarge)

        if (repo.description.isNotBlank()) {
            Text(text = repo.description, style = MaterialTheme.typography.bodyLarge)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.detail_stars, repo.starCount),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(R.string.detail_forks, repo.forkCount),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(R.string.detail_issues, repo.openIssueCount),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        val meta = listOf(repo.language, repo.license).filter { it.isNotBlank() }
        if (meta.isNotEmpty()) {
            Text(
                text = meta.joinToString("  •  "),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (repo.topics.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repo.topics.forEach { topic ->
                    AssistChip(onClick = {}, label = { Text(text = topic) })
                }
            }
        }
    }
}

@Composable
private fun CenterBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) { content() }
}
