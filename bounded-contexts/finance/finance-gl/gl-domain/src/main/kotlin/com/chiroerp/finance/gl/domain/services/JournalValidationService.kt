package com.chiroerp.finance.gl.domain.services

import com.chiroerp.finance.gl.domain.model.JournalEntry

class JournalValidationService {
    fun validate(entry: JournalEntry) {
        entry.ensureBalanced()
    }
}
