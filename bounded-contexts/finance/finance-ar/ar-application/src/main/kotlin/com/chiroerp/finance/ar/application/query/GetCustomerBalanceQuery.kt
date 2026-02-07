package com.chiroerp.finance.ar.application.query

import com.chiroerp.finance.ar.domain.model.CustomerBalance
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.shared.types.cqrs.Query
import java.util.UUID

data class GetCustomerBalanceQuery(
    val tenantId: UUID,
    val customerId: UUID,
    val currency: Currency,
) : Query<CustomerBalance>
