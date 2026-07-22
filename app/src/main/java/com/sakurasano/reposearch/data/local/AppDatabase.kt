package com.sakurasano.reposearch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteRepoEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
