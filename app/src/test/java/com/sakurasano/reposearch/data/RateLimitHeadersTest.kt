package com.sakurasano.reposearch.data

import okhttp3.Headers.Companion.headersOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RateLimitHeadersTest {

    @Test
    fun `retry-afterの秒数を現在時刻に足した時刻になる`() {
        val headers = headersOf("retry-after", "60")

        assertEquals(NOW + 60, retryAtEpochSeconds(headers, NOW))
    }

    @Test
    fun `retry-afterはx-ratelimit-resetより優先される`() {
        val headers = headersOf(
            "retry-after",
            "60",
            "x-ratelimit-remaining",
            "0",
            "x-ratelimit-reset",
            "${NOW + 900}",
        )

        assertEquals(NOW + 60, retryAtEpochSeconds(headers, NOW))
    }

    @Test
    fun `retry-afterが秒数でないときはx-ratelimit-resetを使う`() {
        val headers = headersOf(
            "retry-after",
            "Wed, 21 Oct 2015 07:28:00 GMT",
            "x-ratelimit-remaining",
            "0",
            "x-ratelimit-reset",
            "${NOW + 900}",
        )

        assertEquals(NOW + 900, retryAtEpochSeconds(headers, NOW))
    }

    @Test
    fun `残量が0のときx-ratelimit-resetの時刻になる`() {
        val headers = headersOf(
            "x-ratelimit-remaining",
            "0",
            "x-ratelimit-reset",
            "${NOW + 900}",
        )

        assertEquals(NOW + 900, retryAtEpochSeconds(headers, NOW))
    }

    @Test
    fun `残量が残っているときは時刻を返さない`() {
        val headers = headersOf(
            "x-ratelimit-remaining",
            "58",
            "x-ratelimit-reset",
            "${NOW + 2700}",
        )

        assertNull(retryAtEpochSeconds(headers, NOW))
    }

    @Test
    fun `残量が不明なときは時刻を返さない`() {
        val headers = headersOf("x-ratelimit-reset", "${NOW + 900}")

        assertNull(retryAtEpochSeconds(headers, NOW))
    }

    @Test
    fun `過去の時刻は返さない`() {
        val headers = headersOf(
            "x-ratelimit-remaining",
            "0",
            "x-ratelimit-reset",
            "${NOW - 1}",
        )

        assertNull(retryAtEpochSeconds(headers, NOW))
    }

    @Test
    fun `24時間より先の時刻は返さない`() {
        val headers = headersOf(
            "x-ratelimit-remaining",
            "0",
            "x-ratelimit-reset",
            "${NOW + 24 * 60 * 60 + 1}",
        )

        assertNull(retryAtEpochSeconds(headers, NOW))
    }

    @Test
    fun `retry-afterが0のときは時刻を返さない`() {
        val headers = headersOf("retry-after", "0")

        assertNull(retryAtEpochSeconds(headers, NOW))
    }

    @Test
    fun `ヘッダがないときは時刻を返さない`() {
        assertNull(retryAtEpochSeconds(headersOf(), NOW))
    }

    private companion object {
        const val NOW = 1_787_560_000L
    }
}
