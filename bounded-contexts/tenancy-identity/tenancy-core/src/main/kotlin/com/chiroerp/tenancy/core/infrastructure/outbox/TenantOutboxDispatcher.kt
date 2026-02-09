package com.chiroerp.tenancy.core.infrastructure.outbox

interface TenantOutboxDispatcher {
    fun dispatch(entry: TenantOutboxEntry)
}
