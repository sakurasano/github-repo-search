package com.sakurasano.reposearch.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchHistoryFilterTest {
    @Test
    fun `空クエリのときは全件返す`() {
        val history = listOf("kotlin", "compose", "retrofit")
        assertEquals(history, filterHistory(history, ""))
    }

    @Test
    fun `前方一致で絞り込む`() {
        val history = listOf("kotlin", "kotlinx", "compose")
        assertEquals(listOf("kotlin", "kotlinx"), filterHistory(history, "kotlin"))
    }

    @Test
    fun `大文字小文字を無視する`() {
        val history = listOf("Kotlin", "compose")
        assertEquals(listOf("Kotlin"), filterHistory(history, "kot"))
    }

    @Test
    fun `前方一致しない候補は除外する`() {
        val history = listOf("android-kotlin", "kotlin")
        assertEquals(listOf("kotlin"), filterHistory(history, "kotlin"))
    }

    @Test
    fun `クエリ前後の空白を無視して前方一致する`() {
        val history = listOf("kotlin", "compose")
        assertEquals(listOf("kotlin"), filterHistory(history, "  kot  "))
    }
}
