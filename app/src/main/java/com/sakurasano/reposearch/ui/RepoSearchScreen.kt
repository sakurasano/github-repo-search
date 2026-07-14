package com.sakurasano.reposearch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakurasano.reposearch.R
import com.sakurasano.reposearch.model.RepoSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoSearchScreen(
    onRepoClick: (RepoSummary) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RepoSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.search_clear),
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.search(query) }),
            )

            when (val state = uiState) {
                RepoSearchUiState.Idle -> StatusMessage(
                    icon = Icons.Filled.Search,
                    message = stringResource(R.string.search_prompt),
                )

                RepoSearchUiState.Loading -> LoadingIndicator()

                RepoSearchUiState.Empty -> StatusMessage(
                    icon = Icons.Filled.SearchOff,
                    message = stringResource(R.string.search_empty),
                )

                is RepoSearchUiState.Error -> StatusMessage(
                    icon = Icons.Filled.ErrorOutline,
                    message = stringResource(state.error.messageRes()),
                    onRetry = { viewModel.search(query) },
                    retryLabel = stringResource(R.string.search_retry),
                )

                is RepoSearchUiState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.repos,
                        key = { it.id },
                    ) { repo -> RepoCard(repo, onClick = { onRepoClick(repo) }) }
                }
            }
        }
    }
}

@Composable
private fun RepoCard(repo: RepoSummary, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OwnerAvatar(url = repo.ownerAvatarUrl, size = 40.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = repo.fullName, style = MaterialTheme.typography.titleMedium)
                if (repo.description.isNotBlank()) {
                    Text(
                        text = repo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RepoStat(
                        icon = Icons.Filled.Star,
                        value = repo.starCount.toString(),
                        contentDescription = stringResource(R.string.cd_stars),
                    )
                    if (repo.language.isNotBlank()) {
                        LanguageLabel(language = repo.language)
                    }
                }
            }
        }
    }
}
