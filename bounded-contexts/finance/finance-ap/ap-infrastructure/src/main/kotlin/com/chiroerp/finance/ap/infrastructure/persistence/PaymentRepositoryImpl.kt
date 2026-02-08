package com.chiroerp.finance.ap.infrastructure.persistence

import com.chiroerp.finance.ap.application.service.PaymentRepository
import com.chiroerp.finance.ap.domain.model.BillAllocation
import com.chiroerp.finance.ap.domain.model.Payment
import com.chiroerp.finance.shared.identifiers.PaymentId
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

@ApplicationScoped
class PaymentRepositoryImpl : PanacheRepositoryBase<PaymentEntity, UUID>, PaymentRepository {

    @Transactional
    override fun save(payment: Payment): Payment {
        val existing = list("id = ?1 and tenantId = ?2", payment.id.value, payment.tenantId).firstOrNull()

        val managed = if (existing == null) {
            PaymentEntity.fromDomain(payment).also { persist(it) }
        } else {
            existing.apply {
                vendorId = payment.vendorId
                amount = payment.amount.amount
                currency = payment.amount.currency.code
                paidAt = payment.paidAt
                reference = payment.reference
                status = payment.status
                allocatedAmount = payment.allocatedAmount.amount
            }
        }

        val allocations = loadAllocations(managed.tenantId, managed.id)
        return managed.toDomain(allocations)
    }

    override fun findById(tenantId: UUID, paymentId: PaymentId): Payment? {
        return list("id = ?1 and tenantId = ?2", paymentId.value, tenantId).firstOrNull()
            ?.let { entity -> entity.toDomain(loadAllocations(entity.tenantId, entity.id)) }
    }

    private fun loadAllocations(tenantId: UUID, paymentId: UUID): MutableList<BillAllocation> {
        return getEntityManager()
            .createQuery(
                """
                select allocation
                from BillAllocationEntity allocation
                join allocation.bill bill
                where allocation.paymentId = :paymentId
                  and bill.tenantId = :tenantId
                order by allocation.appliedAt asc, allocation.id asc
                """.trimIndent(),
                BillAllocationEntity::class.java,
            )
            .setParameter("paymentId", paymentId)
            .setParameter("tenantId", tenantId)
            .resultList
            .map { it.toPaymentAllocationDomain() }
            .toMutableList()
    }
}
