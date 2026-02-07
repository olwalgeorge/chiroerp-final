package com.chiroerp.finance.gl.application.port.output

import com.chiroerp.finance.gl.domain.model.JournalEntry
import com.chiroerp.finance.shared.identifiers.JournalEntryId

interface JournalEntryRepository {
    fun save(entry: JournalEntry): JournalEntry
    fun findById(id: JournalEntryId): JournalEntry?
}
