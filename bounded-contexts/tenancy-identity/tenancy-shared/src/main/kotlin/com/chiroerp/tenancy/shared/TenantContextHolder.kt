package com.chiroerp.tenancy.shared

object TenantContextHolder {
    private val context = ThreadLocal<TenantContext?>()

    fun set(current: TenantContext) {
        context.set(current)
    }

    fun get(): TenantContext? = context.get()

    fun require(): TenantContext = context.get()
        ?: throw IllegalStateException("Tenant context is not set")

    fun clear() {
        context.remove()
    }
}
