package com.sakurasano.reposearch.ui

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.sakurasano.reposearch.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ja-rJP")
class AppErrorUiTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var defaultTimeZone: TimeZone

    @Before
    fun setUp() {
        defaultTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(defaultTimeZone)
    }

    @Test
    fun `24時間表示の端末では時刻が24時間表記になる`() {
        setTimeFormat("24")

        assertEquals("17:29", formatRetryAt(context, RETRY_AT))
    }

    @Test
    fun `12時間表示の端末では時刻が12時間表記になる`() {
        setTimeFormat("12")

        assertEquals("午後5:29", formatRetryAt(context, RETRY_AT))
    }

    @Test
    fun `レート制限の文言に時刻が埋め込まれる`() {
        assertEquals(
            "リクエストが多すぎます。17:29以降に再試行できます",
            context.getString(R.string.error_rate_limited_until, "17:29"),
        )
    }

    private fun setTimeFormat(value: String) {
        Settings.System.putString(context.contentResolver, Settings.System.TIME_12_24, value)
    }

    private companion object {
        // 2026-08-24 17:29:48 JST
        const val RETRY_AT = 1_787_560_188L
    }
}
