package com.noobdev.numlexambuddy.model

/**
 * Generic UI state holder for handling loading, success, and error states
 */
sealed class UiState<out T> {
    /**
     * Initial state before loading begins
     */
    object Initial : UiState<Nothing>()
    
    /**
     * Loading state
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Success state with data
     */
    data class Success<T>(override val data: T) : UiState<T>()
    
    /**
     * Error state with message
     */
    data class Error(val message: String) : UiState<Nothing>()
    
    /**
     * Check if this state is [Initial]
     */
    val isInitial: Boolean get() = this is Initial
    
    /**
     * Check if this state is [Loading]
     */
    val isLoading: Boolean get() = this is Loading
    
    /**
     * Check if this state is [Success]
     */
    val isSuccess: Boolean get() = this is Success<*>
    
    /**
     * Check if this state is [Error]
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Get the data if this state is [Success], otherwise return null
     */
    open val data: T? get() = (this as? Success<T>)?.data
    
    /**
     * Get the error message if this state is [Error], otherwise return null
     */
    val errorMessage: String? get() = (this as? Error)?.message
}
