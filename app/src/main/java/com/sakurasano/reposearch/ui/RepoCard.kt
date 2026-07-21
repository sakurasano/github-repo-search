package com.sakurasano.reposearch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sakurasano.reposearch.R
import com.sakurasano.reposearch.model.RepoSummary

/**
 * 検索結果とお気に入り一覧で共通のリポジトリ表示カード。
 * [onToggleFavorite] を渡すと末尾にお気に入りトグルを表示する。
 */
@Composable
fun RepoCard(
    repo: RepoSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OwnerAvatar(url = repo.ownerAvatarUrl, size = 40.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
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
            if (onToggleFavorite != null) {
                IconButton(onClick = onToggleFavorite) {
                    FavoriteToggleIcon(
                        isFavorite = isFavorite,
                        unfavoritedTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
