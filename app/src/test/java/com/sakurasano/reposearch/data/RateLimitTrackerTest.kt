package com.sakurasano.reposearch.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RateLimitTrackerTest {

    private val tracker = RateLimitTracker()

    @Test
    fun `記録前はnullを返す`() {
        assertNull(tracker.snapshot(RateLimitBucket.SEARCH))
    }

    @Test
    fun `searchとcoreは互いを上書きしない`() {
        val search = snapshot(remaining = 3, resetAt = 100)
        val core = snapshot(remaining = 50, resetAt = 200)

        tracker.record(RateLimitBucket.SEARCH, search)
        tracker.record(RateLimitBucket.CORE, core)

        assertEquals(search, tracker.snapshot(RateLimitBucket.SEARCH))
        assertEquals(core, tracker.snapshot(RateLimitBucket.CORE))
    }

    @Test
    fun `同一reset窓では小さい方のremainingが残る`() {
        tracker.record(RateLimitBucket.SEARCH, snapshot(remaining = 5, resetAt = 100))
        tracker.record(RateLimitBucket.SEARCH, snapshot(remaining = 2, resetAt = 100))

        assertEquals(2, tracker.snapshot(RateLimitBucket.SEARCH)?.remaining)

        tracker.record(RateLimitBucket.SEARCH, snapshot(remaining = 8, resetAt = 100))

        assertEquals(2, tracker.snapshot(RateLimitBucket.SEARCH)?.remaining)
    }

    @Test
    fun `古いreset窓の記録は無視される`() {
        tracker.record(RateLimitBucket.SEARCH, snapshot(remaining = 2, resetAt = 200))
        tracker.record(RateLimitBucket.SEARCH, snapshot(remaining = 9, resetAt = 100))

        assertEquals(2, tracker.snapshot(RateLimitBucket.SEARCH)?.remaining)
        assertEquals(200L, tracker.snapshot(RateLimitBucket.SEARCH)?.resetAtEpochSeconds)
    }

    @Test
    fun `新しいreset窓では無条件に置換される`() {
        tracker.record(RateLimitBucket.SEARCH, snapshot(remaining = 0, resetAt = 100))
        tracker.record(RateLimitBucket.SEARCH, snapshot(remaining = 10, resetAt = 200))

        assertEquals(10, tracker.snapshot(RateLimitBucket.SEARCH)?.remaining)
        assertEquals(200L, tracker.snapshot(RateLimitBucket.SEARCH)?.resetAtEpochSeconds)
    }

    private fun snapshot(remaining: Int, resetAt: Long): RateLimitSnapshot =
        RateLimitSnapshot(remaining = remaining, limit = 10, used = 10 - remaining, resetAtEpochSeconds = resetAt)
}
