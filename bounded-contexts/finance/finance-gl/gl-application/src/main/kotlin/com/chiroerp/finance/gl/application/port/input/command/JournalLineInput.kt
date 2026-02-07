package com.chiroerp.finance.gl.application.port.input.command

import com.chiroerp.finance.gl.domain.model.DebitCreditIndicator
import com.chiroerp.finance.shared.identifiers.AccountId
import com.chiroerp.finance.shared.valueobjects.Money

data class JournalLineInput(
    val accountId: AccountId,
    val amount: Money,
    val indicator: DebitCreditIndicator,
    val description: String? = null,
)
