package com.chiroerp.finance.ar.application.service

import com.chiroerp.finance.ar.domain.model.Payment
import com.chiroerp.finance.shared.identifiers.PaymentId
import java.util.UUID

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun findById(tenantId: UUID, paymentId: PaymentId): Payment?
}
