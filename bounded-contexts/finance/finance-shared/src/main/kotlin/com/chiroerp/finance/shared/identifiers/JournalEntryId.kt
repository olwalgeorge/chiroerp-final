package com.chiroerp.finance.shared.identifiers

import java.util.UUID

@JvmInline
value class JournalEntryId(val value: UUID) {
    companion object {
        fun random(): JournalEntryId = JournalEntryId(UUID.randomUUID())
        fun from(value: String): JournalEntryId = JournalEntryId(UUID.fromString(value))
    }
}
