package com.sakurasano.reposearch.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// RoomはAndroid framework上でしか動かないためRobolectricで実行する。
// compileSdkに依らずRobolectricがサポートするSDKへ固定する
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FavoriteDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.favoriteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertした要素がobserveAllに反映される`() = runTest {
        dao.insert(entity(id = 1, savedAt = 100))

        val all = dao.observeAll().first()

        assertEquals(listOf(1L), all.map { it.id })
    }

    @Test
    fun `observeAllはsavedAtの新しい順に並ぶ`() = runTest {
        dao.insert(entity(id = 1, savedAt = 100))
        dao.insert(entity(id = 2, savedAt = 300))
        dao.insert(entity(id = 3, savedAt = 200))

        val ids = dao.observeAll().first().map { it.id }

        assertEquals(listOf(2L, 3L, 1L), ids)
    }

    @Test
    fun `同じidをinsertすると置き換えられ重複しない`() = runTest {
        dao.insert(entity(id = 1, name = "old", savedAt = 100))
        dao.insert(entity(id = 1, name = "new", savedAt = 200))

        val all = dao.observeAll().first()

        assertEquals(1, all.size)
        assertEquals("new", all.first().name)
    }

    @Test
    fun `deleteByIdで指定した要素だけが消える`() = runTest {
        dao.insert(entity(id = 1, savedAt = 100))
        dao.insert(entity(id = 2, savedAt = 200))

        dao.deleteById(1)

        assertEquals(listOf(2L), dao.observeAll().first().map { it.id })
    }

    @Test
    fun `observeFavoriteIdsは登録済みのidを返す`() = runTest {
        dao.insert(entity(id = 1, savedAt = 100))
        dao.insert(entity(id = 2, savedAt = 200))

        assertEquals(setOf(1L, 2L), dao.observeFavoriteIds().first().toSet())
    }

    @Test
    fun `observeIsFavoriteは登録の有無を返す`() = runTest {
        dao.insert(entity(id = 1, savedAt = 100))

        assertTrue(dao.observeIsFavorite(1).first())
        assertFalse(dao.observeIsFavorite(2).first())
    }

    private fun entity(
        id: Long,
        name: String = "repo$id",
        savedAt: Long,
    ) = FavoriteRepoEntity(
        id = id,
        name = name,
        fullName = "owner/$name",
        description = "",
        ownerName = "owner",
        ownerAvatarUrl = "https://example.com/avatar.png",
        starCount = 0,
        language = "Kotlin",
        savedAt = savedAt,
    )
}
