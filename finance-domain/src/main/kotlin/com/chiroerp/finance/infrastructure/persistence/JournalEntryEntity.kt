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
 * JPA Entity for Journal Entry - maps to existing database schema.
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
    
    @Column(name = "entry_date", nullable = false)
    var entryDate: LocalDate = LocalDate.now()
    
    @Column(name = "posting_date", nullable = false)
    var postingDate: LocalDate = LocalDate.now()
    
    @Column(name = "description", nullable = false)
    var description: String = ""
    
    @Column(name = "reference_type", length = 50)
    var referenceType: String? = null
    
    @Column(name = "reference_id")
    var referenceId: UUID? = null
    
    @Column(name = "status", nullable = false, length = 20)
    var status: String = "DRAFT"
    
    @Column(name = "currency_code", nullable = false, length = 3)
    var currencyCode: String = "USD"
    
    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 6)
    var exchangeRate: BigDecimal = BigDecimal.ONE
    
    @Column(name = "reversed_by")
    var reversedBy: UUID? = null
    
    @Column(name = "reversal_entry_id")
    var reversalEntryId: UUID? = null
    
    @Column(name = "created_by", nullable = false)
    var createdBy: UUID = UUID.randomUUID()
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
    
    @Column(name = "updated_by", nullable = false)
    var updatedBy: UUID = UUID.randomUUID()
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
    
    @Column(name = "posted_by")
    var postedBy: UUID? = null
    
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
}

/**
 * JPA Entity for Journal Entry Line - maps to existing database schema.
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
    
    @Column(name = "account_id", nullable = false)
    var accountId: UUID = UUID.randomUUID()
    
    @Column(name = "debit_amount", precision = 18, scale = 2)
    var debitAmount: BigDecimal? = null
    
    @Column(name = "credit_amount", precision = 18, scale = 2)
    var creditAmount: BigDecimal? = null
    
    @Column(name = "description")
    var description: String? = null
    
    @Column(name = "cost_center_id")
    var costCenterId: UUID? = null
    
    @Column(name = "profit_center_id")
    var profitCenterId: UUID? = null
    
    @Column(name = "created_by", nullable = false)
    var createdBy: UUID = UUID.randomUUID()
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
}
