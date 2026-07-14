package com.sakurasano.reposearch.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakurasano.reposearch.R
import com.sakurasano.reposearch.model.RepoSummary

@Composable
fun RepoSearchScreen(
    onRepoClick: (RepoSummary) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RepoSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(modifier = modifier) { innerPadding ->
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
                label = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.search(query) }),
            )

            when (val state = uiState) {
                RepoSearchUiState.Idle -> CenterMessage(stringResource(R.string.search_prompt))

                RepoSearchUiState.Loading -> CenterBox { CircularProgressIndicator() }

                RepoSearchUiState.Empty -> CenterMessage(stringResource(R.string.search_empty))

                is RepoSearchUiState.Error -> CenterMessage(stringResource(state.error.messageRes()))

                is RepoSearchUiState.Success -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = state.repos,
                        key = { it.id },
                    ) { repo -> RepoRow(repo, onClick = { onRepoClick(repo) }) }
                }
            }
        }
    }
}

@Composable
private fun RepoRow(repo: RepoSummary, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = repo.fullName, style = MaterialTheme.typography.titleMedium)
        if (repo.description.isNotBlank()) {
            Text(
                text = repo.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
            )
        }
        Row {
            Text(text = "★ ${repo.starCount}", style = MaterialTheme.typography.labelLarge)
            if (repo.language.isNotBlank()) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = repo.language, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun CenterBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun CenterMessage(text: String) {
    CenterBox { Text(text = text) }
}
