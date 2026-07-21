package com.sakurasano.reposearch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_repos")
data class FavoriteRepoEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val fullName: String,
    val description: String,
    val ownerName: String,
    val ownerAvatarUrl: String,
    val starCount: Int,
    val language: String,
    val savedAt: Long,
)
