package com.sakurasano.reposearch.data

import okhttp3.Headers

enum class RateLimitBucket { SEARCH, CORE }

data class RateLimitSnapshot(
    val remaining: Int,
    val limit: Int,
    val used: Int,
    val resetAtEpochSeconds: Long,
)

/**
 * レスポンスヘッダからレート制限の残量を読み取る。GitHubは`x-ratelimit-*`ヘッダを常に返すとは限らないため、
 * いずれかが欠損しているか数値として不正な場合はnullを返す。
 */
internal fun parseRateLimit(headers: Headers): RateLimitSnapshot? {
    val remaining = headers["x-ratelimit-remaining"]?.toIntOrNull() ?: return null
    val limit = headers["x-ratelimit-limit"]?.toIntOrNull() ?: return null
    val used = headers["x-ratelimit-used"]?.toIntOrNull() ?: return null
    val reset = headers["x-ratelimit-reset"]?.toLongOrNull() ?: return null
    return RateLimitSnapshot(remaining = remaining, limit = limit, used = used, resetAtEpochSeconds = reset)
}

// GitHubは検索APIだけ別バケットのレート制限を持つ
internal fun bucketOf(path: String): RateLimitBucket =
    if (path.startsWith("/search/")) RateLimitBucket.SEARCH else RateLimitBucket.CORE
