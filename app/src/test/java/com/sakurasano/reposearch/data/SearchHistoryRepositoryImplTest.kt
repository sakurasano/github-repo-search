package com.sakurasano.reposearch.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SearchHistoryRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()

    // DataStoreの書き込みをテストと同じ仮想時間で動かし、advanceUntilIdleで完了させられるようにする
    private val dataStoreScope = CoroutineScope(testDispatcher + Job())

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SearchHistoryRepositoryImpl

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { tempFolder.newFile("test.preferences_pb") },
        )
        repository = SearchHistoryRepositoryImpl(dataStore, Json)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun `不正なJSONが保存されていても空リストになる`() = runTest(testDispatcher) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("search_history")] = "これはJSONではない {{{"
        }

        val result = repository.history.first()

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `recordを並行実行しても全キーワードが失われない`() = runTest(testDispatcher) {
        val queries = listOf("kotlin", "android", "compose", "hilt", "retrofit")

        queries.forEach { query ->
            launch { repository.record(query) }
        }
        advanceUntilIdle()

        val result = repository.history.first()
        assertEquals(queries.size, result.size)
        queries.forEach { query ->
            assertTrue("$query が履歴に含まれていない", result.contains(query))
        }
    }

    @Test
    fun `removeで指定した履歴だけが消える`() = runTest(testDispatcher) {
        repository.record("kotlin")
        repository.record("android")
        repository.record("compose")
        advanceUntilIdle()

        repository.remove("android")
        advanceUntilIdle()

        assertEquals(listOf("compose", "kotlin"), repository.history.first())
    }

    @Test
    fun `存在しないクエリのremoveでは履歴が変わらない`() = runTest(testDispatcher) {
        repository.record("kotlin")
        repository.record("android")
        advanceUntilIdle()

        repository.remove("swift")
        advanceUntilIdle()

        assertEquals(listOf("android", "kotlin"), repository.history.first())
    }

    @Test
    fun `clearで履歴が空になる`() = runTest(testDispatcher) {
        repository.record("kotlin")
        repository.record("android")
        advanceUntilIdle()

        repository.clear()
        advanceUntilIdle()

        assertEquals(emptyList<String>(), repository.history.first())
    }
}
