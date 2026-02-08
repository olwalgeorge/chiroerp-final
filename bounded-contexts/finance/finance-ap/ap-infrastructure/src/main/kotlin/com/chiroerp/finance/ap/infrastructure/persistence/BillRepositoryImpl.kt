package com.chiroerp.finance.ap.infrastructure.persistence

import com.chiroerp.finance.ap.application.service.BillRepository
import com.chiroerp.finance.ap.domain.model.Bill
import com.chiroerp.finance.ap.domain.model.BillStatus
import com.chiroerp.finance.shared.identifiers.BillId
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

@ApplicationScoped
class BillRepositoryImpl : PanacheRepositoryBase<BillEntity, UUID>, BillRepository {

    @Transactional
    override fun save(bill: Bill): Bill {
        val existing = list("id = ?1 and tenantId = ?2", bill.id.value, bill.tenantId).firstOrNull()

        val managed = if (existing == null) {
            BillEntity.fromDomain(bill).also { persist(it) }
        } else {
            existing.apply {
                vendorId = bill.vendorId
                billNumber = bill.billNumber
                currency = bill.currency.code
                issueDate = bill.issueDate
                dueDate = bill.dueDate
                status = bill.status

                lines.clear()
                lines.addAll(bill.lines.map { BillLineEntity.fromDomain(it, this) })

                allocations.clear()
                allocations.addAll(bill.allocations.map { BillAllocationEntity.fromDomain(it, this) })
            }
        }

        return managed.toDomain()
    }

    override fun findById(tenantId: UUID, billId: BillId): Bill? {
        return list("id = ?1 and tenantId = ?2", billId.value, tenantId).firstOrNull()?.toDomain()
    }

    override fun listOpenBills(tenantId: UUID): List<Bill> {
        return list("tenantId = ?1 and status in ?2", tenantId, listOf(BillStatus.POSTED, BillStatus.PARTIALLY_PAID))
            .map { it.toDomain() }
    }

    override fun listOpenBillsByVendor(tenantId: UUID, vendorId: UUID): List<Bill> {
        return list("tenantId = ?1 and vendorId = ?2 and status in ?3", tenantId, vendorId, listOf(BillStatus.POSTED, BillStatus.PARTIALLY_PAID))
            .map { it.toDomain() }
    }
}
