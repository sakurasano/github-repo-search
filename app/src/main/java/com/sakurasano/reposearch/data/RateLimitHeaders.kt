package com.sakurasano.reposearch.data

import okhttp3.Headers

// 表示して意味のある待ち時間の上限。これを超える値は異常とみなす
private const val MAX_WAIT_SECONDS = 24L * 60 * 60

/**
 * 再試行してよくなる時刻をエポック秒で返す。判断できなければnull。
 *
 * GitHubは`x-ratelimit-*`をレート制限と無関係なレスポンスにも付けるため、残量が枯渇していることを
 * 確かめてからリセット時刻を採用する。そうしないと権限エラーの403でも待ち時間を表示してしまう。
 */
internal fun retryAtEpochSeconds(headers: Headers, nowEpochSeconds: Long): Long? {
    val retryAt = headers["retry-after"]?.toLongOrNull()?.let { nowEpochSeconds + it }
        ?: headers["x-ratelimit-reset"]?.toLongOrNull()
            ?.takeIf { headers["x-ratelimit-remaining"]?.toIntOrNull() == 0 }
    return retryAt?.takeIf { it > nowEpochSeconds && it - nowEpochSeconds <= MAX_WAIT_SECONDS }
}
