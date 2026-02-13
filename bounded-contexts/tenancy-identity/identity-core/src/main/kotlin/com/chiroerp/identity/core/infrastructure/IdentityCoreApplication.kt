package com.chiroerp.identity.core.infrastructure

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.annotations.QuarkusMain

/**
 * Identity Core bootstrap aligned with ADR-005 tenant scope and ADR-007 security strategy.
 *
 * Context-specific identity semantics stay in identity-core/tenancy-shared.
 * Platform-shared dependencies are technical only (ADR-006 governance).
 */
@QuarkusMain
class IdentityCoreApplication

fun main(args: Array<String>) {
    Quarkus.run(*args)
    Quarkus.waitForExit()
}
