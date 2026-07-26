package com.sakurasano.reposearch.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakurasano.reposearch.model.SearchSort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SearchSortRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()

    // DataStoreの書き込みをテストと同じ仮想時間で動かし、advanceUntilIdleで完了させられるようにする
    private val dataStoreScope = CoroutineScope(testDispatcher + Job())

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SearchSortRepositoryImpl

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { tempFolder.newFile("test.preferences_pb") },
        )
        repository = SearchSortRepositoryImpl(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun `保存した並び順が読み出せる`() = runTest(testDispatcher) {
        repository.setSortOption(SearchSort.STARS)
        advanceUntilIdle()

        assertEquals(SearchSort.STARS, repository.sortOption.first())
    }

    @Test
    fun `未保存ならベストマッチになる`() = runTest(testDispatcher) {
        assertEquals(SearchSort.BEST_MATCH, repository.sortOption.first())
    }

    @Test
    fun `未知の値が保存されていてもベストマッチになる`() = runTest(testDispatcher) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("search_sort")] = "UNKNOWN_VALUE"
        }

        assertEquals(SearchSort.BEST_MATCH, repository.sortOption.first())
    }
}
