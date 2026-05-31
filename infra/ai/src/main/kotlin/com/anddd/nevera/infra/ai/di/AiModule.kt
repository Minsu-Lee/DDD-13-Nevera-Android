package com.anddd.nevera.infra.ai.di

import android.content.Context
import com.anddd.nevera.core.ui.ai.GemmaDownloadConfirmationLauncher
import com.anddd.nevera.domain.repository.GemmaModelRepository
import com.anddd.nevera.domain.repository.GemmaPromptRepository
import com.anddd.nevera.domain.usecase.ai.GetGemmaModelPathUseCase
import com.anddd.nevera.infra.ai.confirmation.GemmaDownloadConfirmationLauncherImpl
import com.anddd.nevera.infra.ai.datasource.PlayAiPackDataSource
import com.anddd.nevera.infra.ai.datasource.PlayAiPackDataSourceImpl
import com.anddd.nevera.infra.ai.engine.GemmaInferenceEngine
import com.anddd.nevera.infra.ai.engine.LiteRtGemmaInferenceEngine
import com.anddd.nevera.infra.ai.image.GemmaImageNormalizer
import com.anddd.nevera.infra.ai.merger.GemmaShardMerger
import com.anddd.nevera.infra.ai.merger.ShardMerger
import com.anddd.nevera.infra.ai.repository.GemmaModelRepositoryImpl
import com.anddd.nevera.infra.ai.repository.GemmaPromptRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindGemmaPromptRepository(impl: GemmaPromptRepositoryImpl): GemmaPromptRepository

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

        @Provides
        @Singleton
        fun provideGemmaImageNormalizer(@ApplicationContext context: Context): GemmaImageNormalizer =
            GemmaImageNormalizer(
                context = context,
                outputDir = File(context.cacheDir, "gemma4/images"),
            )

        @Provides
        @Singleton
        fun provideGemmaInferenceEngine(
            getGemmaModelPath: GetGemmaModelPathUseCase,
            imageNormalizer: GemmaImageNormalizer,
            @ApplicationContext context: Context,
        ): GemmaInferenceEngine = LiteRtGemmaInferenceEngine(
            getGemmaModelPath = getGemmaModelPath,
            imageNormalizer = imageNormalizer,
            cacheDir = context.cacheDir,
        )
    }
}
