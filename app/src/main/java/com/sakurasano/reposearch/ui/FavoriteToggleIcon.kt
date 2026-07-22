package com.sakurasano.reposearch.ui

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
 * お気に入り状態を表すブックマークアイコン。
 */
@Composable
fun FavoriteToggleIcon(
    isFavorite: Boolean,
    unfavoritedTint: Color = LocalContentColor.current,
) {
    Icon(
        imageVector = ImageVector.vectorResource(
            if (isFavorite) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border,
        ),
        contentDescription = stringResource(
            if (isFavorite) R.string.cd_favorite_remove else R.string.cd_favorite_add,
        ),
        tint = if (isFavorite) MaterialTheme.colorScheme.primary else unfavoritedTint,
    )
}
