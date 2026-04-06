package com.anddd.nevera.data.datasource

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalSessionDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FakeSessionDataSource
