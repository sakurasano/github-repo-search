package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class ApiCallTest {

    @Test
    fun `blockが正常値を返すとSuccessになる`() = runTest {
        val result = apiCall { 42 }

        assertEquals(DataResult.Success(42), result)
    }

    @Test
    fun `IOExceptionを投げるとNetworkになる`() = runTest {
        val result = apiCall { throw IOException() }

        assertEquals(DataResult.Failure(AppError.Network), result)
    }

    @Test
    fun `HttpExceptionの403でRateLimitedになる`() = runTest {
        val result = apiCall { throw httpException(403) }

        assertEquals(DataResult.Failure(AppError.RateLimited(null)), result)
    }

    @Test
    fun `HttpExceptionの429でRateLimitedになる`() = runTest {
        val result = apiCall { throw httpException(429) }

        assertEquals(DataResult.Failure(AppError.RateLimited(null)), result)
    }

    @Test
    fun `レート制限が枯渇しているとRateLimitedが再試行できる時刻を持つ`() = runTest {
        val retryAt = System.currentTimeMillis() / 1000 + 900
        val headers = headersOf(
            "x-ratelimit-remaining",
            "0",
            "x-ratelimit-reset",
            "$retryAt",
        )

        val result = apiCall { throw httpException(429, headers) }

        assertEquals(DataResult.Failure(AppError.RateLimited(retryAt)), result)
    }

    @Test
    fun `レート制限が枯渇していない403は時刻を持たない`() = runTest {
        val headers = headersOf(
            "x-ratelimit-remaining",
            "58",
            "x-ratelimit-reset",
            "${System.currentTimeMillis() / 1000 + 900}",
        )

        val result = apiCall { throw httpException(403, headers) }

        assertEquals(DataResult.Failure(AppError.RateLimited(null)), result)
    }

    @Test
    fun `HttpExceptionの403と429以外でServerになる`() = runTest {
        val result = apiCall { throw httpException(500) }

        assertEquals(DataResult.Failure(AppError.Server(500)), result)
    }

    @Test
    fun `CancellationExceptionは飲み込まず再throwされる`() = runTest {
        val token = CancellationException("cancel")

        val thrown = try {
            apiCall { throw token }
            null
        } catch (e: CancellationException) {
            e
        }

        assertEquals(token, thrown)
    }

    @Test
    fun `その他の例外はUnknownになる`() = runTest {
        val cause = RuntimeException("想定外")

        val result = apiCall { throw cause }

        assertEquals(DataResult.Failure(AppError.Unknown(cause)), result)
    }

    private fun httpException(code: Int, headers: Headers = headersOf()): HttpException {
        val raw = okhttp3.Response.Builder()
            .request(Request.Builder().url("https://api.github.com/").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("error")
            .headers(headers)
            .build()
        return HttpException(Response.error<Any>("".toResponseBody(), raw))
    }
}
