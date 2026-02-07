package com.chiroerp.finance.gl.application.port.input.command

import com.chiroerp.finance.shared.JournalEntryType
import com.chiroerp.finance.shared.identifiers.LedgerId
import com.chiroerp.shared.types.cqrs.Command
import java.util.UUID

data class PostJournalEntryCommand(
    val tenantId: UUID,
    val ledgerId: LedgerId,
    val type: JournalEntryType,
    val reference: String,
    val lines: List<JournalLineInput>,
) : Command
