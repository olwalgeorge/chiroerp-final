package com.chiroerp.tenancy.core.infrastructure

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.annotations.QuarkusMain

/**
 * Tenancy Core bootstrap aligned with ADR-005 (tenant isolation).
 *
 * Domain/business semantics remain in tenancy-shared and tenancy-core modules.
 * Platform-shared is used only for technical primitives per ADR-006.
 */
@QuarkusMain
class TenancyCoreApplication

fun main(args: Array<String>) {
    Quarkus.run(*args)
}
