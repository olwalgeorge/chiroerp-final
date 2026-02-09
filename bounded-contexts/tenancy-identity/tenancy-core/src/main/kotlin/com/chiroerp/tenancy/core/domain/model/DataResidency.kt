package com.chiroerp.tenancy.core.domain.model

data class DataResidency(val countryCode: String) {
    init {
        require(COUNTRY_CODE_REGEX.matches(countryCode)) {
            "Country code must be ISO-3166 alpha-2"
        }
    }

    companion object {
        private val COUNTRY_CODE_REGEX = Regex("^[A-Z]{2}$")
    }
}
