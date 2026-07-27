package com.sakurasano.reposearch.data

import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// android.util.LogはRobolectric上でしか動かないため
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RateLimitInterceptorTest {

    @Test
    fun `レスポンスヘッダをTrackerに記録する`() {
        val tracker = RateLimitTracker()
        val interceptor = RateLimitInterceptor(tracker)
        val request = Request.Builder().url("https://api.github.com/search/repositories").build()
        val headers = headersOf(
            "x-ratelimit-remaining",
            "9",
            "x-ratelimit-limit",
            "10",
            "x-ratelimit-used",
            "1",
            "x-ratelimit-reset",
            "1700000000",
        )

        interceptor.intercept(FakeChain(request, headers))

        assertEquals(
            RateLimitSnapshot(remaining = 9, limit = 10, used = 1, resetAtEpochSeconds = 1700000000),
            tracker.snapshot(RateLimitBucket.SEARCH),
        )
    }

    @Test
    fun `ヘッダが解析できないレスポンスは記録しない`() {
        val tracker = RateLimitTracker()
        val interceptor = RateLimitInterceptor(tracker)
        val request = Request.Builder().url("https://api.github.com/repos/o/r").build()

        interceptor.intercept(FakeChain(request, headersOf()))

        assertEquals(null, tracker.snapshot(RateLimitBucket.CORE))
    }

    private class FakeChain(
        private val request: Request,
        private val responseHeaders: Headers,
    ) : Interceptor.Chain {
        override fun request(): Request = request

        override fun proceed(request: Request): Response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .headers(responseHeaders)
            .build()

        override fun connection() = null
        override fun call() = throw UnsupportedOperationException()
        override fun connectTimeoutMillis() = 0
        override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun readTimeoutMillis() = 0
        override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun writeTimeoutMillis() = 0
        override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
    }
}
