package com.noobdev.numlexambuddy.data.di

import android.content.Context
import com.noobdev.numlexambuddy.service.GeminiService
import com.noobdev.numlexambuddy.service.GeminiServiceImpl
import com.noobdev.numlexambuddy.utils.GeminiErrorHandler
import com.noobdev.numlexambuddy.utils.PromptGenerator

/**
 * Dependency Injection module for providing AI services
 */
object AIServiceModule {
    
    /**
     * Provides a GeminiService implementation
     */
    fun provideGeminiService(context: Context): GeminiService {
        val errorHandler = GeminiErrorHandler()
        val promptGenerator = PromptGenerator()
        return GeminiServiceImpl(context, errorHandler, promptGenerator)
    }
}
