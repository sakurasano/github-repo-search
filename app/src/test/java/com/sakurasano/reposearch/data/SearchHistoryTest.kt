package com.sakurasano.reposearch.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchHistoryTest {

    @Test
    fun `新しいキーワードが先頭に追加される`() {
        val result = listOf("kotlin", "android").withRecorded("compose")

        assertEquals(listOf("compose", "kotlin", "android"), result)
    }

    @Test
    fun `既存の同一キーワードは重複せず先頭へ繰り上がる`() {
        val result = listOf("kotlin", "android", "compose").withRecorded("android")

        assertEquals(listOf("android", "kotlin", "compose"), result)
    }

    @Test
    fun `大文字小文字違いは重複として扱い最新表記を残す`() {
        val result = listOf("Kotlin", "android").withRecorded("KOTLIN")

        assertEquals(listOf("KOTLIN", "android"), result)
    }

    @Test
    fun `上限を超えたら最古が押し出され件数が上限に収まる`() {
        val result = listOf("a", "b", "c").withRecorded("new", limit = 3)

        assertEquals(listOf("new", "a", "b"), result)
    }

    @Test
    fun `trim後空のクエリは記録されない`() {
        val base = listOf("kotlin", "android")

        val result = base.withRecorded("   ")

        assertEquals(base, result)
    }
}
