package com.chiroerp.finance.infrastructure.persistence

import com.chiroerp.finance.domain.JournalEntry
import com.chiroerp.finance.domain.JournalEntryLine
import com.chiroerp.finance.domain.JournalEntryStatus
import com.chiroerp.finance.domain.JournalEntryType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * JPA Entity for Journal Entry - aligns with domain model.
 */
@Entity
@Table(name = "journal_entries", schema = "finance")
class JournalEntryEntity : PanacheEntityBase {

    @Id
    var id: UUID = UUID.randomUUID()

    @Column(name = "entry_number", nullable = false, length = 50)
    var entryNumber: String = ""

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID = UUID.randomUUID()

    @Column(name = "company_code", nullable = false, length = 20)
    var companyCode: String = ""

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    var entryType: JournalEntryType = JournalEntryType.STANDARD

    @Column(name = "document_date", nullable = false)
    var documentDate: LocalDate = LocalDate.now()

    @Column(name = "posting_date", nullable = false)
    var postingDate: LocalDate = LocalDate.now()

    @Column(name = "fiscal_year", nullable = false)
    var fiscalYear: Int = LocalDate.now().year

    @Column(name = "fiscal_period", nullable = false)
    var fiscalPeriod: Int = LocalDate.now().monthValue

    @Column(name = "description", nullable = false)
    var description: String = ""

    @Column(name = "reference_type", length = 50)
    var referenceType: String? = null

    @Column(name = "reference_id")
    var referenceId: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: JournalEntryStatus = JournalEntryStatus.DRAFT

    @Column(name = "currency_code", nullable = false, length = 3)
    var currencyCode: String = "USD"

    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 6)
    var exchangeRate: BigDecimal = BigDecimal.ONE

    @Column(name = "reversed_by")
    var reversedBy: String? = null

    @Column(name = "reversal_entry_id")
    var reversalEntryId: String? = null

    @Column(name = "created_by", nullable = false)
    var createdBy: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_by", nullable = false)
    var updatedBy: String = ""

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

    @Column(name = "posted_by")
    var postedBy: String? = null

    @Column(name = "posted_at")
    var postedAt: Instant? = null

    @Version
    var version: Long = 0L

    @OneToMany(
        mappedBy = "journalEntry",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @OrderBy("lineNumber ASC")
    var lines: MutableList<JournalEntryLineEntity> = mutableListOf()

    fun toDomain(): JournalEntry {
        val domainLines = lines.map { it.toDomain() }
        return JournalEntry(
            entryId = id.toString(),
            entryNumber = entryNumber,
            companyCode = companyCode,
            entryType = entryType,
            documentDate = documentDate,
            postingDate = postingDate,
            fiscalYear = fiscalYear,
            fiscalPeriod = fiscalPeriod,
            currency = currencyCode,
            exchangeRate = exchangeRate,
            description = description,
            reference = referenceId,
            lines = domainLines,
            status = status,
            createdBy = createdBy,
            createdAt = createdAt,
            postedBy = postedBy,
            postedAt = postedAt,
            reversedBy = reversedBy,
            reversalEntryId = reversalEntryId
        )
    }

    companion object {
        fun fromDomain(domain: JournalEntry, tenantId: UUID): JournalEntryEntity {
            val entity = JournalEntryEntity()
            entity.id = UUID.fromString(domain.entryId)
            entity.tenantId = tenantId
            entity.entryNumber = domain.entryNumber
            entity.companyCode = domain.companyCode
            entity.entryType = domain.entryType
            entity.documentDate = domain.documentDate
            entity.postingDate = domain.postingDate
            entity.fiscalYear = domain.fiscalYear
            entity.fiscalPeriod = domain.fiscalPeriod
            entity.description = domain.description
            entity.referenceId = domain.reference
            entity.referenceType = null
            entity.status = domain.status
            entity.currencyCode = domain.currency
            entity.exchangeRate = domain.exchangeRate
            entity.reversedBy = domain.reversedBy
            entity.reversalEntryId = domain.reversalEntryId
            entity.createdBy = domain.createdBy
            entity.createdAt = domain.createdAt
            entity.updatedBy = domain.createdBy
            entity.updatedAt = domain.createdAt
            entity.postedBy = domain.postedBy
            entity.postedAt = domain.postedAt

            entity.lines = domain.lines.map { JournalEntryLineEntity.fromDomain(it, entity, tenantId) }.toMutableList()
            return entity
        }
    }
}

/**
 * JPA Entity for Journal Entry Line.
 */
@Entity
@Table(name = "journal_entry_lines", schema = "finance")
class JournalEntryLineEntity : PanacheEntityBase {

    @Id
    var id: UUID = UUID.randomUUID()

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    var journalEntry: JournalEntryEntity? = null

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID = UUID.randomUUID()

    @Column(name = "line_number", nullable = false)
    var lineNumber: Int = 0

    @Column(name = "account_number", nullable = false, length = 50)
    var accountNumber: String = ""

    @Column(name = "debit_amount", precision = 18, scale = 2)
    var debitAmount: BigDecimal? = null

    @Column(name = "credit_amount", precision = 18, scale = 2)
    var creditAmount: BigDecimal? = null

    @Column(name = "description")
    var description: String? = null

    @Column(name = "cost_center")
    var costCenter: String? = null

    @Column(name = "profit_center")
    var profitCenter: String? = null

    @Column(name = "created_by", nullable = false)
    var createdBy: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    fun toDomain(): JournalEntryLine {
        return JournalEntryLine(
            lineId = id.toString(),
            lineNumber = lineNumber,
            accountNumber = accountNumber,
            debitAmount = debitAmount ?: BigDecimal.ZERO,
            creditAmount = creditAmount ?: BigDecimal.ZERO,
            costCenter = costCenter,
            profitCenter = profitCenter,
            text = description
        )
    }

    companion object {
        fun fromDomain(
            domain: JournalEntryLine,
            entry: JournalEntryEntity,
            tenantId: UUID
        ): JournalEntryLineEntity {
            val entity = JournalEntryLineEntity()
            entity.id = UUID.fromString(domain.lineId)
            entity.journalEntry = entry
            entity.tenantId = tenantId
            entity.lineNumber = domain.lineNumber
            entity.accountNumber = domain.accountNumber
            entity.debitAmount = domain.debitAmount
            entity.creditAmount = domain.creditAmount
            entity.description = domain.text
            entity.costCenter = domain.costCenter
            entity.profitCenter = domain.profitCenter
            entity.createdBy = entry.createdBy
            entity.createdAt = entry.createdAt
            return entity
        }
    }
}
