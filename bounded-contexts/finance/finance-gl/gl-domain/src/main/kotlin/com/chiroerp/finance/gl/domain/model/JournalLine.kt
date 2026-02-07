package com.chiroerp.finance.gl.domain.model

import com.chiroerp.finance.shared.identifiers.AccountId
import com.chiroerp.finance.shared.valueobjects.Money

data class JournalLine(
    val accountId: AccountId,
    val amount: Money,
    val indicator: DebitCreditIndicator,
    val description: String? = null,
)
