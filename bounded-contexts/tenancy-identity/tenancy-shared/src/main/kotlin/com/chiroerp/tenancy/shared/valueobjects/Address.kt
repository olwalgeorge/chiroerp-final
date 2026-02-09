package com.chiroerp.tenancy.shared.valueobjects

data class Address(
    val line1: String,
    val city: String,
    val countryCode: String,
    val postalCode: String? = null,
)
