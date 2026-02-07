package com.chiroerp.tenancy.shared

/**
 * Thread-local tenant context used by request-bound processing.
 */
object TenantContextHolder {
    private val context: ThreadLocal<TenantContext> = ThreadLocal()

    fun set(current: TenantContext) {
        context.set(current)
    }

    fun get(): TenantContext? = context.get()

    fun requireCurrent(): TenantContext = get()
        ?: error("Tenant context is not set for current thread")

    fun clear() {
        context.remove()
    }

    fun <T> withContext(current: TenantContext, block: () -> T): T {
        set(current)
        return try {
            block()
        } finally {
            clear()
        }
    }
}
