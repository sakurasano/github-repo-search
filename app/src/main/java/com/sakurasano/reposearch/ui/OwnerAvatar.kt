package com.sakurasano.reposearch.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage

/**
 * オーナーのavatarを円形で表示する。
 * URLが空、または読み込みに失敗した場合はプレースホルダの円を表示し、空白の円にならないようにする。
 */
@Composable
fun OwnerAvatar(url: String, size: Dp, modifier: Modifier = Modifier) {
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    AsyncImage(
        model = url.ifBlank { null },
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
    )
}
