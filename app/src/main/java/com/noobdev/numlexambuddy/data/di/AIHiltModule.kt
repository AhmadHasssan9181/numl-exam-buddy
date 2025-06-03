package com.noobdev.numlexambuddy.data.di

import android.content.Context
import com.noobdev.numlexambuddy.service.GeminiService
import com.noobdev.numlexambuddy.service.GeminiServiceImpl
import com.noobdev.numlexambuddy.utils.GeminiErrorHandler
import com.noobdev.numlexambuddy.utils.PromptGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIHiltModule {

    @Provides
    @Singleton
    fun provideGeminiErrorHandler(): GeminiErrorHandler {
        return GeminiErrorHandler()
    }

    @Provides
    @Singleton
    fun providePromptGenerator(): PromptGenerator {
        return PromptGenerator()
    }

    @Provides
    @Singleton
    fun provideGeminiService(
        @ApplicationContext context: Context,
        errorHandler: GeminiErrorHandler,
        promptGenerator: PromptGenerator
    ): GeminiService {
        return GeminiServiceImpl(context, errorHandler, promptGenerator)
    }
}
