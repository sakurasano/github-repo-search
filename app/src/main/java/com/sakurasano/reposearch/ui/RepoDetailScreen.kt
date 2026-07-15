package com.sakurasano.reposearch.ui

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakurasano.reposearch.R
import com.sakurasano.reposearch.model.RepoDetail
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RepoDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val openError = stringResource(R.string.detail_open_in_browser_error)

    val title = when (val state = uiState) {
        is RepoDetailUiState.Success -> state.repo.name
        else -> stringResource(R.string.detail_title)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                actions = {
                    val state = uiState
                    if (state is RepoDetailUiState.Success && state.repo.htmlUrl.isNotBlank()) {
                        IconButton(
                            onClick = {
                                if (!openInCustomTab(context, state.repo.htmlUrl)) {
                                    scope.launch { snackbarHostState.showSnackbar(openError) }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_open_in_new),
                                contentDescription = stringResource(R.string.cd_open_in_browser),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            RepoDetailUiState.Loading -> LoadingIndicator(Modifier.padding(innerPadding))

            is RepoDetailUiState.Error -> StatusMessage(
                icon = ImageVector.vectorResource(R.drawable.ic_error_outline),
                message = stringResource(state.error.messageRes()),
                modifier = Modifier.padding(innerPadding),
                onRetry = viewModel::retry,
                retryLabel = stringResource(R.string.detail_retry),
            )

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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OwnerAvatar(url = repo.ownerAvatarUrl, size = 48.dp)
            Text(text = repo.ownerName, style = MaterialTheme.typography.bodyMedium)
        }
        Text(text = repo.fullName, style = MaterialTheme.typography.titleLarge)

        if (repo.description.isNotBlank()) {
            Text(
                text = repo.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                RepoStat(
                    icon = Icons.Filled.Star,
                    value = repo.starCount.toString(),
                    contentDescription = stringResource(R.string.cd_stars),
                )
                RepoStat(
                    icon = ImageVector.vectorResource(R.drawable.ic_fork_right),
                    value = repo.forkCount.toString(),
                    contentDescription = stringResource(R.string.cd_forks),
                )
                RepoStat(
                    icon = ImageVector.vectorResource(R.drawable.ic_bug_report),
                    value = repo.openIssueCount.toString(),
                    contentDescription = stringResource(R.string.cd_issues),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (repo.language.isNotBlank()) {
                LanguageLabel(language = repo.language)
            }
            if (repo.license.isNotBlank()) {
                RepoStat(
                    icon = ImageVector.vectorResource(R.drawable.ic_description),
                    value = repo.license,
                    contentDescription = stringResource(R.string.cd_license),
                )
            }
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

private fun openInCustomTab(context: Context, url: String): Boolean = try {
    CustomTabsIntent.Builder().build().launchUrl(context, url.toUri())
    true
} catch (e: ActivityNotFoundException) {
    false
}
