package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.RepoDetail
import com.sakurasano.reposearch.model.RepoSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteMapperTest {

    @Test
    fun `RepoSummaryはEntityへ変換して戻しても内容が保たれる`() {
        val summary = RepoSummary(
            id = 42,
            name = "nowinandroid",
            fullName = "android/nowinandroid",
            description = "説明",
            ownerName = "android",
            ownerAvatarUrl = "https://example.com/avatar.png",
            starCount = 100,
            language = "Kotlin",
        )

        val restored = summary.toFavoriteEntity(savedAt = 1_000).toDomain()

        assertEquals(summary, restored)
    }

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
