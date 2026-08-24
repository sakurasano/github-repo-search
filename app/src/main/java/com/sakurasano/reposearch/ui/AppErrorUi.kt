package com.sakurasano.reposearch.ui

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.sakurasano.reposearch.R
import com.sakurasano.reposearch.model.AppError
import java.util.Date

/**
 * 表示する文言を1つの`when`で決める。リソースIDの選択と書式引数の受け渡しを分けると、
 * 片方だけ変えたときに`%1$s`が生のまま画面に出てしまい、Lintでも検出できないため。
 */
@Composable
fun AppError.message(): String = when (this) {
    AppError.Network -> stringResource(R.string.error_network)

    is AppError.RateLimited ->
        retryAtEpochSeconds
            ?.let { stringResource(R.string.error_rate_limited_until, formatRetryAt(LocalContext.current, it)) }
            ?: stringResource(R.string.error_rate_limited)

    is AppError.Server -> stringResource(R.string.error_server)

    is AppError.Unknown -> stringResource(R.string.error_unknown)
}

// 端末の12/24時間設定に従う。返るSimpleDateFormatは非スレッドセーフなので使い捨てる
internal fun formatRetryAt(context: Context, epochSeconds: Long): String =
    DateFormat.getTimeFormat(context).format(Date(epochSeconds * 1000))
