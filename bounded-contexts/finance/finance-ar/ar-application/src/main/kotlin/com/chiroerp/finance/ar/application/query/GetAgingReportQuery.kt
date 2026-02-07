package com.chiroerp.finance.ar.application.query

import com.chiroerp.finance.ar.domain.model.AgingReport
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.shared.types.cqrs.Query
import java.time.LocalDate
import java.util.UUID

data class GetAgingReportQuery(
    val tenantId: UUID,
    val asOfDate: LocalDate,
    val currency: Currency,
) : Query<AgingReport>
