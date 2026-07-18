package com.sakurasano.reposearch.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.sakurasano.reposearch.R

/**
 * お気に入り状態を表す★アイコン。検索カードと詳細画面で共有する。
 */
@Composable
fun FavoriteToggleIcon(
    isFavorite: Boolean,
    unfavoritedTint: Color = LocalContentColor.current,
) {
    Icon(
        imageVector = if (isFavorite) {
            Icons.Filled.Star
        } else {
            ImageVector.vectorResource(R.drawable.ic_star_border)
        },
        contentDescription = stringResource(
            if (isFavorite) R.string.cd_favorite_remove else R.string.cd_favorite_add,
        ),
        tint = if (isFavorite) MaterialTheme.colorScheme.primary else unfavoritedTint,
    )
}
