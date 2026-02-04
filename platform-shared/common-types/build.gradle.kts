/*
 * Platform Shared - Common Types Module
 *
 * This module provides shared value objects, domain events, and CQRS base types
 * used across all bounded contexts in ChiroERP.
 *
 * Architecture: ADR-001 (CQRS), ADR-006 (Platform Shared Governance)
 * Pattern: Shared Kernel (DDD)
 */

plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    // No external framework dependencies - keep domain pure
    // Only Kotlin stdlib and utilities

    implementation(libs.vavr)                    // Functional types (Option, Either, Try)
    implementation(libs.commons.lang3)           // String/date utilities

    // Testing
    testImplementation(libs.bundles.testing.core)
    testImplementation(libs.bundles.testing.kotest)
}

// Architecture validation
val validateArchitectureTask = tasks.register("validateArchitecture") {
    group = "verification"
    description = "Ensures common-types has no framework dependencies"

    // Make configuration-cache compatible by capturing values at configuration time
    val implementationDeps = configurations.getByName("implementation").dependencies
        .map { "${it.group}:${it.name}" }

    doLast {
        val violations = implementationDeps.filter { dep ->
            dep.contains("quarkus") ||
            dep.contains("spring") ||
            dep.contains("jakarta.persistence")
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "❌ ARCHITECTURE VIOLATION: common-types must not depend on frameworks!\n" +
                "Violating dependencies: $violations\n" +
                "See ADR-006: Platform Shared Governance"
            )
        }

        println("✓ Architecture validation passed: No framework dependencies")
    }
}

tasks.named("build") {
    dependsOn(validateArchitectureTask)
}
