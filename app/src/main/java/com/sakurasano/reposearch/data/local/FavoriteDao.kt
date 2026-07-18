package com.sakurasano.reposearch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_repos ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteRepoEntity>>

    @Query("SELECT id FROM favorite_repos")
    fun observeFavoriteIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_repos WHERE id = :id)")
    fun observeIsFavorite(id: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_repos WHERE id = :id)")
    suspend fun existsById(id: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteRepoEntity)

    @Query("DELETE FROM favorite_repos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
