package com.chiroerp.finance.infrastructure.persistence

import com.chiroerp.finance.domain.JournalEntry
import com.chiroerp.finance.domain.JournalEntryLine
import com.chiroerp.finance.domain.JournalEntryStatus
import com.chiroerp.finance.domain.JournalEntryType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * JPA Entity for Journal Entry persistence.
 * 
 * Maps the JournalEntry domain model to the database schema.
 * Follows the repository pattern with Panache for data access.
 * 
 * Related ADRs:
 * - ADR-009: Financial Accounting Domain
 * - ADR-002: Database per Bounded Context
 */
@Entity
@Table(name = "journal_entries", schema = "finance")
class JournalEntryEntity : PanacheEntityBase {
    
    @Id
    @Column(name = "entry_id", nullable = false, length = 50)
    var entryId: String = UUID.randomUUID().toString()
    
    @Column(name = "entry_number", nullable = false, unique = true, length = 20)
    var entryNumber: String = ""
    
    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID = UUID.randomUUID()
    
    @Column(name = "company_code", nullable = false, length = 20)
    var companyCode: String = ""
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 50)
    var entryType: JournalEntryType = JournalEntryType.STANDARD
    
    @Column(name = "document_date", nullable = false)
    var documentDate: LocalDate = LocalDate.now()
    
    @Column(name = "posting_date", nullable = false)
    var postingDate: LocalDate = LocalDate.now()
    
    @Column(name = "fiscal_year", nullable = false)
    var fiscalYear: Int = 0
    
    @Column(name = "fiscal_period", nullable = false)
    var fiscalPeriod: Int = 0
    
    @Column(name = "currency", nullable = false, length = 3)
    var currency: String = "USD"
    
    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 6)
    var exchangeRate: BigDecimal = BigDecimal.ONE
    
    @Column(name = "description", nullable = false, length = 500)
    var description: String = ""
    
    @Column(name = "reference", length = 100)
    var reference: String? = null
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: JournalEntryStatus = JournalEntryStatus.DRAFT
    
    @Column(name = "created_by", nullable = false, length = 100)
    var createdBy: String = ""
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
    
    @Column(name = "posted_by", length = 100)
    var postedBy: String? = null
    
    @Column(name = "posted_at")
    var postedAt: Instant? = null
    
    @Column(name = "reversed_by", length = 100)
    var reversedBy: String? = null
    
    @Column(name = "reversal_entry_id", length = 50)
    var reversalEntryId: String? = null
    
    @OneToMany(
        mappedBy = "journalEntry",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @OrderBy("lineNumber ASC")
    var lines: MutableList<JournalEntryLineEntity> = mutableListOf()
    
    /**
     * Convert JPA entity to domain model.
     */
    fun toDomain(): JournalEntry {
        return JournalEntry(
            entryId = entryId,
            entryNumber = entryNumber,
            companyCode = companyCode,
            entryType = entryType,
            documentDate = documentDate,
            postingDate = postingDate,
            fiscalYear = fiscalYear,
            fiscalPeriod = fiscalPeriod,
            currency = currency,
            exchangeRate = exchangeRate,
            description = description,
            reference = reference,
            lines = lines.map { it.toDomain() },
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
        /**
         * Create JPA entity from domain model.
         */
        fun fromDomain(journalEntry: JournalEntry, tenantId: UUID): JournalEntryEntity {
            val entity = JournalEntryEntity().apply {
                this.entryId = journalEntry.entryId
                this.entryNumber = journalEntry.entryNumber
                this.tenantId = tenantId
                this.companyCode = journalEntry.companyCode
                this.entryType = journalEntry.entryType
                this.documentDate = journalEntry.documentDate
                this.postingDate = journalEntry.postingDate
                this.fiscalYear = journalEntry.fiscalYear
                this.fiscalPeriod = journalEntry.fiscalPeriod
                this.currency = journalEntry.currency
                this.exchangeRate = journalEntry.exchangeRate
                this.description = journalEntry.description
                this.reference = journalEntry.reference
                this.status = journalEntry.status
                this.createdBy = journalEntry.createdBy
                this.createdAt = journalEntry.createdAt
                this.postedBy = journalEntry.postedBy
                this.postedAt = journalEntry.postedAt
                this.reversedBy = journalEntry.reversedBy
                this.reversalEntryId = journalEntry.reversalEntryId
            }
            
            // Convert lines
            entity.lines = journalEntry.lines.map { line ->
                JournalEntryLineEntity.fromDomain(line, entity)
            }.toMutableList()
            
            return entity
        }
    }
}

/**
 * JPA Entity for Journal Entry Line persistence.
 */
@Entity
@Table(name = "journal_entry_lines", schema = "finance")
class JournalEntryLineEntity : PanacheEntityBase {
    
    @Id
    @Column(name = "line_id", nullable = false, length = 50)
    var lineId: String = UUID.randomUUID().toString()
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_id", nullable = false)
    var journalEntry: JournalEntryEntity? = null
    
    @Column(name = "line_number", nullable = false)
    var lineNumber: Int = 0
    
    @Column(name = "account_number", nullable = false, length = 20)
    var accountNumber: String = ""
    
    @Column(name = "debit_amount", nullable = false, precision = 18, scale = 2)
    var debitAmount: BigDecimal = BigDecimal.ZERO
    
    @Column(name = "credit_amount", nullable = false, precision = 18, scale = 2)
    var creditAmount: BigDecimal = BigDecimal.ZERO
    
    @Column(name = "cost_center", length = 20)
    var costCenter: String? = null
    
    @Column(name = "profit_center", length = 20)
    var profitCenter: String? = null
    
    @Column(name = "business_area", length = 20)
    var businessArea: String? = null
    
    @Column(name = "text", length = 200)
    var text: String? = null
    
    @Column(name = "assignment", length = 100)
    var assignment: String? = null
    
    @Column(name = "tax_code", length = 20)
    var taxCode: String? = null
    
    @Column(name = "quantity", precision = 18, scale = 4)
    var quantity: BigDecimal? = null
    
    @Column(name = "uom", length = 10)
    var uom: String? = null
    
    /**
     * Convert JPA entity to domain model.
     */
    fun toDomain(): JournalEntryLine {
        return JournalEntryLine(
            lineId = lineId,
            lineNumber = lineNumber,
            accountNumber = accountNumber,
            debitAmount = debitAmount,
            creditAmount = creditAmount,
            costCenter = costCenter,
            profitCenter = profitCenter,
            businessArea = businessArea,
            text = text,
            assignment = assignment,
            taxCode = taxCode,
            quantity = quantity,
            uom = uom
        )
    }
    
    companion object {
        /**
         * Create JPA entity from domain model.
         */
        fun fromDomain(line: JournalEntryLine, journalEntry: JournalEntryEntity): JournalEntryLineEntity {
            return JournalEntryLineEntity().apply {
                this.lineId = line.lineId
                this.journalEntry = journalEntry
                this.lineNumber = line.lineNumber
                this.accountNumber = line.accountNumber
                this.debitAmount = line.debitAmount
                this.creditAmount = line.creditAmount
                this.costCenter = line.costCenter
                this.profitCenter = line.profitCenter
                this.businessArea = line.businessArea
                this.text = line.text
                this.assignment = line.assignment
                this.taxCode = line.taxCode
                this.quantity = line.quantity
                this.uom = line.uom
            }
        }
    }
}
