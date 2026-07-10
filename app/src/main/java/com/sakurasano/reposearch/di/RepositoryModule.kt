package com.sakurasano.reposearch.di

import com.sakurasano.reposearch.data.RepoSearchRepository
import com.sakurasano.reposearch.data.RepoSearchRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindRepoSearchRepository(impl: RepoSearchRepositoryImpl): RepoSearchRepository
}
