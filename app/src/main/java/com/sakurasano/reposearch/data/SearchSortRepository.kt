package com.sakurasano.reposearch.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakurasano.reposearch.di.SettingsDataStore
import com.sakurasano.reposearch.model.SearchSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

interface SearchSortRepository {
    val sortOption: Flow<SearchSort>

    suspend fun setSortOption(sort: SearchSort)
}

class SearchSortRepositoryImpl @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>,
) : SearchSortRepository {

    override val sortOption: Flow<SearchSort> = dataStore.data
        .catch { cause ->
            // 設定が読めなくても検索はできるべきなので、既定値へフォールバックする
            if (cause is IOException) emit(emptyPreferences()) else throw cause
        }
        .map { preferences -> preferences[SORT_KEY].toSearchSort() }

    override suspend fun setSortOption(sort: SearchSort) {
        dataStore.edit { preferences -> preferences[SORT_KEY] = sort.name }
    }

    private fun String?.toSearchSort(): SearchSort =
        SearchSort.entries.firstOrNull { it.name == this } ?: SearchSort.BEST_MATCH

    private companion object {
        val SORT_KEY = stringPreferencesKey("search_sort")
    }
}
