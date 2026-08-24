package com.sakurasano.reposearch.data

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * バケットごとのレート制限残量を保持する。
 * 並行リクエストの応答を到着順に上書きすると、古い応答が新しい応答の`remaining`を巻き戻してしまう。
 * そのためリセット時刻の新しい応答を優先し、同じ時刻なら残量の少ないほうを新しいとみなす。
 */
@Singleton
class RateLimitTracker @Inject constructor() {

    private val snapshots = ConcurrentHashMap<RateLimitBucket, RateLimitSnapshot>()

    fun record(bucket: RateLimitBucket, snapshot: RateLimitSnapshot) {
        snapshots.compute(bucket) { _, current ->
            when {
                current == null -> snapshot
                snapshot.resetAtEpochSeconds > current.resetAtEpochSeconds -> snapshot
                snapshot.resetAtEpochSeconds < current.resetAtEpochSeconds -> current
                else -> if (snapshot.remaining < current.remaining) snapshot else current
            }
        }
    }

    fun snapshot(bucket: RateLimitBucket): RateLimitSnapshot? = snapshots[bucket]
}
