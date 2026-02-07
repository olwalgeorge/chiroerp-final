package com.chiroerp.shared.types.results

sealed interface Result<out T> {
    data class Success<T>(val value: T) : Result<T>
    data class Failure(val error: DomainError) : Result<Nothing>

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> default()
    }

    companion object {
        fun <T> success(value: T): Result<T> = Success(value)
        fun failure(error: DomainError): Result<Nothing> = Failure(error)
    }
}
