package com.sakurasano.reposearch.di

import com.sakurasano.reposearch.data.RepoDetailRepository
import com.sakurasano.reposearch.data.RepoDetailRepositoryImpl
import com.sakurasano.reposearch.data.RepoSearchRepository
import com.sakurasano.reposearch.data.RepoSearchRepositoryImpl
import com.sakurasano.reposearch.data.ThemeRepository
import com.sakurasano.reposearch.data.ThemeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindRepoSearchRepository(impl: RepoSearchRepositoryImpl): RepoSearchRepository

    @Binds
    abstract fun bindRepoDetailRepository(impl: RepoDetailRepositoryImpl): RepoDetailRepository

    @Binds
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
}
