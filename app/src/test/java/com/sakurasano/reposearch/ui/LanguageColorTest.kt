package com.sakurasano.reposearch.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageColorTest {
    @Test
    fun `既知の言語は対応する色を返す`() {
        assertEquals(Color(0xFFA97BFF), languageColor("Kotlin"))
    }

    @Test
    fun `大文字小文字を無視して照合する`() {
        assertEquals(languageColor("kotlin"), languageColor("KOTLIN"))
    }

    @Test
    fun `未知の言語はnullを返す`() {
        assertNull(languageColor("NotARealLanguage"))
    }

    @Test
    fun `空文字はnullを返す`() {
        assertNull(languageColor(""))
    }
}
