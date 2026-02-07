package com.chiroerp.finance.ap.infrastructure.persistence

import com.chiroerp.finance.ap.domain.model.Payment
import com.chiroerp.finance.ap.domain.model.PaymentStatus
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "payments")
class PaymentEntity : PanacheEntityBase() {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    lateinit var id: UUID

    @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
    lateinit var tenantId: UUID

    @Column(name = "vendor_id", columnDefinition = "uuid", nullable = false)
    lateinit var vendorId: UUID

    @Column(name = "amount", nullable = false, precision = 19, scale = 6)
    lateinit var amount: BigDecimal

    @Column(name = "currency", nullable = false)
    lateinit var currency: String

    @Column(name = "paid_at", nullable = false)
    lateinit var paidAt: LocalDate

    @Column(name = "reference", nullable = false)
    lateinit var reference: String

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    lateinit var status: PaymentStatus

    @Column(name = "allocated_amount", nullable = false, precision = 19, scale = 6)
    var allocatedAmount: BigDecimal = BigDecimal.ZERO

    fun toDomain(): Payment {
        return Payment.reconstruct(
            id = PaymentId(id),
            tenantId = tenantId,
            vendorId = vendorId,
            amount = Money.of(amount, Currency.of(currency)),
            paidAt = paidAt,
            reference = reference,
            allocations = mutableListOf(), // TODO: load from separate table if needed
            status = status,
        )
    }

    companion object {
        fun fromDomain(payment: Payment): PaymentEntity {
            val entity = PaymentEntity()
            entity.id = payment.id.value
            entity.tenantId = payment.tenantId
            entity.vendorId = payment.vendorId
            entity.amount = payment.amount.amount
            entity.currency = payment.amount.currency.code
            entity.paidAt = payment.paidAt
            entity.reference = payment.reference
            entity.status = payment.status
            entity.allocatedAmount = payment.allocatedAmount.amount
            return entity
        }
    }
}