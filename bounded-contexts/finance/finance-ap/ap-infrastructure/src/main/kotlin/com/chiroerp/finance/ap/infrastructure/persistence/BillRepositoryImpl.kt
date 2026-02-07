package com.chiroerp.finance.ap.infrastructure.persistence

import com.chiroerp.finance.ap.application.service.BillRepository
import com.chiroerp.finance.ap.domain.model.Bill
import com.chiroerp.finance.ap.domain.model.BillStatus
import com.chiroerp.finance.shared.identifiers.BillId
import io.quarkus.hibernate.orm.panache.PanacheQuery
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class BillRepositoryImpl : PanacheRepositoryBase<BillEntity, UUID>, BillRepository {

    override fun save(bill: Bill): Bill {
        val entity = BillEntity.fromDomain(bill)
        persist(entity)
        return entity.toDomain()
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