package com.chiroerp.finance.ap.application.service

import com.chiroerp.finance.ap.domain.model.Bill
import com.chiroerp.finance.shared.identifiers.BillId
import java.util.UUID

interface BillRepository {
    fun save(bill: Bill): Bill
    fun findById(tenantId: UUID, billId: BillId): Bill?
    fun listOpenBills(tenantId: UUID): List<Bill>
    fun listOpenBillsByVendor(tenantId: UUID, vendorId: UUID): List<Bill>
}