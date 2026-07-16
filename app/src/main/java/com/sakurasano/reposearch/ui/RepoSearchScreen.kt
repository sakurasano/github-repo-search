package com.sakurasano.reposearch.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakurasano.reposearch.R
import com.sakurasano.reposearch.model.RepoSummary
import com.sakurasano.reposearch.model.ThemeMode

internal fun filterHistory(history: List<String>, query: String): List<String> =
    if (query.isBlank()) {
        history
    } else {
        history.filter { it.startsWith(query.trim(), ignoreCase = true) }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoSearchScreen(
    onRepoClick: (RepoSummary) -> Unit,
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier,
    repoSearchViewModel: RepoSearchViewModel = hiltViewModel(),
) {
    val uiState by repoSearchViewModel.uiState.collectAsStateWithLifecycle()
    val history by repoSearchViewModel.history.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val suggestions = remember(query, history) { filterHistory(history, query) }

    // IMEを閉じてもフォーカスが残りサジェストが結果を覆い続けるのを防ぐ逃げ道
    BackHandler(enabled = isFocused) { focusManager.clearFocus() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = { ThemeMenu(themeViewModel) },
            )
        },
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
                interactionSource = interactionSource,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        repoSearchViewModel.search(query)
                    },
                ),
            )

            if (isFocused && suggestions.isNotEmpty()) {
                SearchHistorySuggestions(
                    suggestions = suggestions,
                    onSelect = {
                        query = it
                        focusManager.clearFocus()
                        repoSearchViewModel.search(it)
                    },
                    onRemove = { repoSearchViewModel.removeHistory(it) },
                    onClearAll = { repoSearchViewModel.clearHistory() },
                )
            } else {
                when (val state = uiState) {
                    RepoSearchUiState.Idle -> StatusMessage(
                        icon = Icons.Filled.Search,
                        message = stringResource(R.string.search_prompt),
                    )

                    RepoSearchUiState.Loading -> LoadingIndicator()

                    RepoSearchUiState.Empty -> StatusMessage(
                        icon = ImageVector.vectorResource(R.drawable.ic_search_off),
                        message = stringResource(R.string.search_empty),
                    )

                    is RepoSearchUiState.Error -> StatusMessage(
                        icon = ImageVector.vectorResource(R.drawable.ic_error_outline),
                        message = stringResource(state.error.messageRes()),
                        onRetry = { repoSearchViewModel.search(query) },
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
}

@Composable
private fun SearchHistorySuggestions(
    suggestions: List<String>,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        items(items = suggestions, key = { it }) { keyword ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(keyword) }
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_history),
                    contentDescription = null,
                )
                Text(
                    text = keyword,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                IconButton(onClick = { onRemove(keyword) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_search_history_remove),
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.search_history_clear_all),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClearAll() }
                    .heightIn(min = 48.dp)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ThemeMenu(viewModel: ThemeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentMode = (uiState as? ThemeUiState.Success)?.themeMode
    var expanded by rememberSaveable { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_dark_mode),
            contentDescription = stringResource(R.string.cd_theme),
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        ThemeMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = { Text(stringResource(mode.labelRes())) },
                onClick = {
                    viewModel.setThemeMode(mode)
                    expanded = false
                },
                leadingIcon = { RadioButton(selected = mode == currentMode, onClick = null) },
            )
        }
    }
}

@StringRes
private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
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
