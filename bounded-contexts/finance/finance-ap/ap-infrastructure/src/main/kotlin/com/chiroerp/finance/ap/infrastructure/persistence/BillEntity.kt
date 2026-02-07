package com.chiroerp.finance.ap.infrastructure.persistence

import com.chiroerp.finance.ap.domain.model.Bill
import com.chiroerp.finance.ap.domain.model.BillLine
import com.chiroerp.finance.ap.domain.model.BillStatus
import com.chiroerp.finance.ap.domain.model.PaymentAllocation
import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "bills")
class BillEntity : PanacheEntityBase() {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    lateinit var id: UUID

    @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
    lateinit var tenantId: UUID

    @Column(name = "vendor_id", columnDefinition = "uuid", nullable = false)
    lateinit var vendorId: UUID

    @Column(name = "bill_number", nullable = false)
    lateinit var billNumber: String

    @Column(name = "currency", nullable = false)
    lateinit var currency: String

    @Column(name = "issue_date", nullable = false)
    lateinit var issueDate: LocalDate

    @Column(name = "due_date", nullable = false)
    lateinit var dueDate: LocalDate

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    lateinit var status: BillStatus

    @OneToMany(mappedBy = "bill", cascade = [CascadeType.ALL], orphanRemoval = true)
    lateinit var lines: MutableList<BillLineEntity>

    @OneToMany(mappedBy = "bill", cascade = [CascadeType.ALL], orphanRemoval = true)
    lateinit var allocations: MutableList<BillAllocationEntity>

    fun toDomain(): Bill {
        return Bill.reconstruct(
            id = BillId(id),
            tenantId = tenantId,
            vendorId = vendorId,
            billNumber = billNumber,
            currency = Currency.of(currency),
            issueDate = issueDate,
            dueDate = dueDate,
            lines = lines.map { it.toDomain() },
            allocations = allocations.map { it.toDomain() }.toMutableList(),
            status = status,
        )
    }

    companion object {
        fun fromDomain(bill: Bill): BillEntity {
            val entity = BillEntity()
            entity.id = bill.id.value
            entity.tenantId = bill.tenantId
            entity.vendorId = bill.vendorId
            entity.billNumber = bill.billNumber
            entity.currency = bill.currency.code
            entity.issueDate = bill.issueDate
            entity.dueDate = bill.dueDate
            entity.status = bill.status
            entity.lines = bill.lines.map { BillLineEntity.fromDomain(it, entity) }.toMutableList()
            entity.allocations = bill.allocations.map { BillAllocationEntity.fromDomain(it, entity) }.toMutableList()
            return entity
        }
    }
}

@Entity
@Table(name = "bill_lines")
class BillLineEntity : PanacheEntityBase() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    lateinit var id: UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    lateinit var bill: BillEntity

    @Column(name = "description", nullable = false)
    lateinit var description: String

    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    lateinit var quantity: BigDecimal

    @Column(name = "unit_price_amount", nullable = false, precision = 19, scale = 6)
    lateinit var unitPriceAmount: BigDecimal

    @Column(name = "unit_price_currency", nullable = false)
    lateinit var unitPriceCurrency: String

    fun toDomain(): BillLine = BillLine(
        description = description,
        quantity = quantity,
        unitPrice = Money.of(unitPriceAmount, Currency.of(unitPriceCurrency)),
    )

    companion object {
        fun fromDomain(line: BillLine, bill: BillEntity): BillLineEntity {
            val entity = BillLineEntity()
            entity.bill = bill
            entity.description = line.description
            entity.quantity = line.quantity
            entity.unitPriceAmount = line.unitPrice.amount
            entity.unitPriceCurrency = line.unitPrice.currency.code
            return entity
        }
    }
}

@Entity
@Table(name = "bill_allocations")
class BillAllocationEntity : PanacheEntityBase() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    lateinit var id: UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    lateinit var bill: BillEntity

    @Column(name = "payment_id", columnDefinition = "uuid", nullable = false)
    lateinit var paymentId: UUID

    @Column(name = "amount", nullable = false, precision = 19, scale = 6)
    lateinit var amount: BigDecimal

    @Column(name = "currency", nullable = false)
    lateinit var currency: String

    @Column(name = "applied_at", nullable = false)
    lateinit var appliedAt: LocalDate

    fun toDomain(): PaymentAllocation = PaymentAllocation(
        paymentId = com.chiroerp.finance.shared.identifiers.PaymentId(paymentId),
        amount = Money.of(amount, Currency.of(currency)),
        appliedAt = appliedAt,
    )

    companion object {
        fun fromDomain(allocation: PaymentAllocation, bill: BillEntity): BillAllocationEntity {
            val entity = BillAllocationEntity()
            entity.bill = bill
            entity.paymentId = allocation.paymentId.value
            entity.amount = allocation.amount.amount
            entity.currency = allocation.amount.currency.code
            entity.appliedAt = allocation.appliedAt
            return entity
        }
    }
}