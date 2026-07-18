package com.sakurasano.reposearch.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sakurasano.reposearch.data.local.AppDatabase
import com.sakurasano.reposearch.model.RepoSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FavoriteRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: FavoriteRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = FavoriteRepositoryImpl(database.favoriteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `未登録のリポジトリをお気に入りにすると登録される`() = runTest {
        repository.toggle(sampleRepo())

        assertTrue(repository.observeIsFavorite(SAMPLE_ID).first())
    }

    @Test
    fun `登録済みのリポジトリをお気に入り操作すると解除される`() = runTest {
        repository.toggle(sampleRepo())

        repository.toggle(sampleRepo())

        assertFalse(repository.observeIsFavorite(SAMPLE_ID).first())
    }

    @Test
    fun `同じリポジトリを続けて2回操作すると元の状態に戻る`() = runTest {
        repository.toggle(sampleRepo()) // 登録
        repository.toggle(sampleRepo()) // 解除

        assertFalse(repository.observeIsFavorite(SAMPLE_ID).first())
    }

    private fun sampleRepo() = RepoSummary(
        id = SAMPLE_ID,
        name = "nowinandroid",
        fullName = "android/nowinandroid",
        description = "",
        ownerName = "android",
        ownerAvatarUrl = "https://example.com/avatar.png",
        starCount = 100,
        language = "Kotlin",
    )

    private companion object {
        const val SAMPLE_ID = 1L
    }
}
