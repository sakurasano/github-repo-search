package com.sakurasano.reposearch.data

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
}
