package com.chiroerp.tenancy.shared.valueobjects

data class Address(
    val line1: String,
    val line2: String? = null,
    val city: String,
    val stateOrProvince: String? = null,
    val postalCode: String,
    val countryCode: String,
) {
    init {
        require(line1.isNotBlank()) { "Address line 1 cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(postalCode.isNotBlank()) { "Postal code cannot be blank" }
        require(countryCode.length == 2) { "Country code must be ISO alpha-2" }
    }
}
