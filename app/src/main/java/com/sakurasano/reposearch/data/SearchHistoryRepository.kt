package com.sakurasano.reposearch.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakurasano.reposearch.di.SearchHistoryDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

private const val MAX_HISTORY = 10

interface SearchHistoryRepository {
    val history: Flow<List<String>>

    suspend fun record(query: String)

    suspend fun remove(query: String)

    suspend fun clear()
}

class SearchHistoryRepositoryImpl @Inject constructor(
    @param:SearchHistoryDataStore private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : SearchHistoryRepository {

    override val history: Flow<List<String>> = dataStore.data
        .catch { cause ->
            // 履歴が読めなくても検索機能は使えるべきなので、空履歴へフォールバックする
            if (cause is IOException) emit(emptyPreferences()) else throw cause
        }
        .map { preferences -> decodeHistory(preferences) }
        .distinctUntilChanged()

    override suspend fun record(query: String) {
        // editブロック内で読み直して書き戻し、並行実行でも取りこぼさない（トランザクションはプロセス内保証）
        dataStore.edit { preferences ->
            preferences[HISTORY_KEY] = json.encodeToString(decodeHistory(preferences).withRecorded(query))
        }
    }

    override suspend fun remove(query: String) {
        dataStore.edit { preferences ->
            preferences[HISTORY_KEY] = json.encodeToString(decodeHistory(preferences) - query)
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences -> preferences.remove(HISTORY_KEY) }
    }

    private fun decodeHistory(preferences: Preferences): List<String> =
        preferences[HISTORY_KEY]?.let { stored ->
            runCatching { json.decodeFromString<List<String>>(stored) }.getOrDefault(emptyList())
        } ?: emptyList()

    private companion object {
        val HISTORY_KEY = stringPreferencesKey("search_history")
    }
}

/**
 * 履歴へクエリを反映した新しいリストを返す
 * 前後空白を除いて空なら記録せず、大文字小文字を無視して既存の重複を除いたうえで最新表記を先頭に置き、上限件数に収める
 */
internal fun List<String>.withRecorded(query: String, limit: Int = MAX_HISTORY): List<String> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return this

    return (listOf(trimmed) + this.filterNot { it.equals(trimmed, ignoreCase = true) })
        .take(limit)
}
