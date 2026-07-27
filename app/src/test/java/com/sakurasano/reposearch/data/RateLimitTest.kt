package com.sakurasano.reposearch.data

import okhttp3.Headers.Companion.headersOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RateLimitTest {

    @Test
    fun `正常なヘッダから残量を読み取れる`() {
        val headers = headersOf(
            "x-ratelimit-remaining",
            "5",
            "x-ratelimit-limit",
            "10",
            "x-ratelimit-used",
            "5",
            "x-ratelimit-reset",
            "1700000000",
        )

        val snapshot = parseRateLimit(headers)

        assertEquals(
            RateLimitSnapshot(remaining = 5, limit = 10, used = 5, resetAtEpochSeconds = 1700000000),
            snapshot,
        )
    }

    @Test
    fun `ヘッダが欠損しているとnullになる`() {
        val headers = headersOf(
            "x-ratelimit-remaining",
            "5",
            "x-ratelimit-limit",
            "10",
        )

        assertNull(parseRateLimit(headers))
    }

    @Test
    fun `ヘッダの値が数値でないとnullになる`() {
        val headers = headersOf(
            "x-ratelimit-remaining",
            "invalid",
            "x-ratelimit-limit",
            "10",
            "x-ratelimit-used",
            "5",
            "x-ratelimit-reset",
            "1700000000",
        )

        assertNull(parseRateLimit(headers))
    }

    @Test
    fun `検索エンドポイントのパスはSEARCHバケットになる`() {
        assertEquals(RateLimitBucket.SEARCH, bucketOf("/search/repositories"))
    }

    @Test
    fun `検索以外のパスはCOREバケットになる`() {
        assertEquals(RateLimitBucket.CORE, bucketOf("/repos/o/r"))
    }
}
