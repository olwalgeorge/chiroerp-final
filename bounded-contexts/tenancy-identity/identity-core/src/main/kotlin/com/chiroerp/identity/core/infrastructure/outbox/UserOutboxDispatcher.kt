package com.chiroerp.identity.core.infrastructure.outbox

interface UserOutboxDispatcher {
    fun dispatch(entry: UserOutboxEntry)
}
