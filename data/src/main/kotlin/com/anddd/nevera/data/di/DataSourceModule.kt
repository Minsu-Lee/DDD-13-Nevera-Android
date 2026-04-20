package com.anddd.nevera.data.di

import com.anddd.nevera.data.datasource.AndroidKeyStoreProvider
import com.anddd.nevera.data.datasource.FcmTokenDataSource
import com.anddd.nevera.data.datasource.FcmTokenDataSourceImpl
import com.anddd.nevera.data.datasource.KeyProvider
import com.anddd.nevera.data.datasource.NotificationDataSource
import com.anddd.nevera.data.datasource.NotificationDataSourceImpl
import com.anddd.nevera.data.datasource.TokenDataSourceImpl
import com.anddd.nevera.data.datasource.RefreshDataSource
import com.anddd.nevera.data.datasource.RefreshDataSourceImpl
import com.anddd.nevera.data.datasource.TokenDataSource
import com.anddd.nevera.data.datasource.UserDataSourceImpl
import com.anddd.nevera.data.datasource.UserDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindKeyProvider(impl: AndroidKeyStoreProvider): KeyProvider

    @Binds
    @Singleton
    abstract fun bindTokenDataSource(impl: TokenDataSourceImpl): TokenDataSource

    @Binds
    @Singleton
    abstract fun bindUserDataSource(impl: UserDataSourceImpl): UserDataSource

    @Binds
    @Singleton
    abstract fun bindRefreshDataSource(impl: RefreshDataSourceImpl): RefreshDataSource

    @Binds
    @Singleton
    abstract fun bindFcmTokenDataSource(impl: FcmTokenDataSourceImpl): FcmTokenDataSource

    @Binds
    @Singleton
    abstract fun bindNotificationDataSource(impl: NotificationDataSourceImpl): NotificationDataSource
}
