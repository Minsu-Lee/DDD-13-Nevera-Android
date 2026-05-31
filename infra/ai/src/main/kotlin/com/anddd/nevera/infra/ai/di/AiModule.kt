package com.anddd.nevera.infra.ai.di

import android.content.Context
import com.anddd.nevera.core.ui.ai.GemmaDownloadConfirmationLauncher
import com.anddd.nevera.domain.repository.GemmaModelRepository
import com.anddd.nevera.infra.ai.confirmation.GemmaDownloadConfirmationLauncherImpl
import com.anddd.nevera.infra.ai.datasource.PlayAiPackDataSource
import com.anddd.nevera.infra.ai.datasource.PlayAiPackDataSourceImpl
import com.anddd.nevera.infra.ai.merger.GemmaShardMerger
import com.anddd.nevera.infra.ai.merger.ShardMerger
import com.anddd.nevera.infra.ai.repository.GemmaModelRepositoryImpl
import com.google.android.play.core.aipacks.AiPackManager
import com.google.android.play.core.aipacks.AiPackManagerFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindGemmaModelRepository(impl: GemmaModelRepositoryImpl): GemmaModelRepository

    @Binds
    @Singleton
    abstract fun bindGemmaDownloadConfirmationLauncher(
        impl: GemmaDownloadConfirmationLauncherImpl,
    ): GemmaDownloadConfirmationLauncher

    companion object {

        @Provides
        @Singleton
        fun provideAiPackManager(@ApplicationContext context: Context): AiPackManager =
            AiPackManagerFactory.getInstance(context)

        @Provides
        @Singleton
        fun providePlayAiPackDataSource(manager: AiPackManager): PlayAiPackDataSource =
            PlayAiPackDataSourceImpl(manager)

        @Provides
        @Singleton
        fun provideShardMerger(@ApplicationContext context: Context): ShardMerger =
            GemmaShardMerger(outputDir = File(context.noBackupFilesDir, "gemma4"))
    }
}
