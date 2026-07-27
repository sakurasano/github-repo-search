package com.sakurasano.reposearch.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

private const val TAG = "RateLimitInterceptor"

/**
 * 全レスポンスからレート制限ヘッダを読み取り[RateLimitTracker]へ記録する。リクエストは改変しない。
 */
class RateLimitInterceptor @Inject constructor(
    private val tracker: RateLimitTracker,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val snapshot = parseRateLimit(response.headers)
        if (snapshot != null) {
            val bucket = bucketOf(request.url.encodedPath)
            tracker.record(bucket, snapshot)
            Log.d(TAG, "bucket=$bucket remaining=${snapshot.remaining} used=${snapshot.used}")
        }
        return response
    }
}
