package com.chiroerp.shared.types.results

data class ValidationError(
    override val code: String,
    override val message: String,
    val field: String? = null,
) : DomainError
