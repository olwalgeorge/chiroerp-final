package com.chiroerp.tenancy.core.domain.model

import java.time.LocalDate

data class TenantSubscription(
    val planCode: String = "STANDARD",
    val renewalDate: LocalDate? = null,
)
