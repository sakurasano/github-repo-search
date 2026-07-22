package com.sakurasano.reposearch.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaginationTest {

    @Test
    fun `総件数がページ末尾を超えていれば続きがある`() {
        assertTrue(hasMorePages(page = 1, perPage = 30, totalCount = 100))
        assertTrue(hasMorePages(page = 2, perPage = 30, totalCount = 100))
    }

    @Test
    fun `取得済みが総件数に達したら続きはない`() {
        assertFalse(hasMorePages(page = 4, perPage = 30, totalCount = 100))
        assertFalse(hasMorePages(page = 1, perPage = 30, totalCount = 30))
        assertFalse(hasMorePages(page = 1, perPage = 30, totalCount = 10))
    }

    @Test
    fun `総件数が膨大でも到達上限の1000件で打ち切る`() {
        // page33までで990件、上限1000件までまだ余地がある
        assertTrue(hasMorePages(page = 33, perPage = 30, totalCount = 45_000))
        // page34で1020件となり上限1000件を超えるため、それ以上は要求しない
        assertFalse(hasMorePages(page = 34, perPage = 30, totalCount = 45_000))
    }
}
