package com.sakurasano.reposearch.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RepoDetailTest {

    @Test
    fun `RepoDetailからRepoSummaryへ変換できidが引き継がれる`() {
        val detail = RepoDetail(
            id = 7,
            name = "nowinandroid",
            fullName = "android/nowinandroid",
            htmlUrl = "https://github.com/android/nowinandroid",
            description = "説明",
            ownerName = "android",
            ownerAvatarUrl = "https://example.com/avatar.png",
            starCount = 100,
            forkCount = 10,
            openIssueCount = 3,
            language = "Kotlin",
            topics = listOf("compose"),
            license = "Apache License 2.0",
        )

        val summary = detail.toSummary()

        assertEquals(7L, summary.id)
        assertEquals("android/nowinandroid", summary.fullName)
        assertEquals("Kotlin", summary.language)
    }
}
