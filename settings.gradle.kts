/*
 * ChiroERP - Cloud-Native Multi-Tenant ERP Platform
 * Settings Configuration - Gradle 9.0 with Kotlin DSL
 *
 * This file configures the multi-project build structure with automatic
 * module discovery for all bounded contexts and platform services.
 *
 * Architecture: 92 modules across 12 domains (see docs/architecture/README.md)
 * Build System: Gradle 9.0 + Kotlin DSL + Quarkus 3.29.0 + Java 21
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    // Include convention plugins from build-logic
    includeBuild("build-logic")
}

// Enable type-safe project accessors (e.g., projects.platformShared.commonTypes)
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Enable stable configuration cache
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "chiroerp"

// Centralized dependency management via version catalog
// Note: Gradle 9.x automatically loads gradle/libs.versions.toml as "libs" catalog
dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS prevents modules from declaring their own repositories
    // All repositories must be declared here for consistency and security
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()

        // Confluent for Kafka/Avro if needed
        maven {
            name = "ConfluentRepository"
            url = uri("https://packages.confluent.io/maven/")
        }
    }
}

// =============================================================================
// AUTOMATIC MODULE DISCOVERY
// =============================================================================
// This block automatically includes all modules by walking the directory tree
// and finding all build.gradle.kts files. This eliminates manual includes.
//
// Excluded directories: build, .gradle, build-logic, buildSrc
// =============================================================================

val excludedDirs = setOf("build", ".gradle", "build-logic", "buildSrc", ".git", ".github", "docs", "scripts")

fun includeModulesRecursively(dir: File, parentPath: String = "") {
    dir.listFiles()?.forEach { file ->
        if (file.isDirectory && file.name !in excludedDirs) {
            val buildFile = File(file, "build.gradle.kts")

            if (buildFile.exists()) {
                // Module found - include it
                val modulePath = if (parentPath.isEmpty()) {
                    ":${file.name}"
                } else {
                    "$parentPath:${file.name}"
                }
                include(modulePath)
                println("✓ Discovered module: $modulePath")
            }

            // Recurse into subdirectories
            val nextPath = if (parentPath.isEmpty()) {
                ":${file.name}"
            } else {
                "$parentPath:${file.name}"
            }
            includeModulesRecursively(file, nextPath)
        }
    }
}

// Start discovery from root directory
println("\n=== ChiroERP Module Discovery ===")
includeModulesRecursively(rootDir)
println("=== Discovery Complete ===\n")

// =============================================================================
// MANUAL INCLUDES (Optional - for explicit control)
// =============================================================================
// If you prefer explicit module declarations, comment out the automatic
// discovery above and manually include modules here:
//
// // Platform Shared Libraries
// include(":platform-shared:common-types")
// include(":platform-shared:common-messaging")
// include(":platform-shared:common-security")
// include(":platform-shared:common-observability")
// include(":platform-shared:config-engine")
//
// // Platform Infrastructure
// include(":platform-infrastructure:cqrs")
// include(":platform-infrastructure:eventing")
// include(":platform-infrastructure:monitoring")
//
// // Finance Domain (7 services - ADR-009)
// include(":bounded-contexts:finance:finance-gl")
// include(":bounded-contexts:finance:finance-ap")
// include(":bounded-contexts:finance:finance-ar")
// include(":bounded-contexts:finance:finance-assets")
// include(":bounded-contexts:finance:finance-treasury")
// include(":bounded-contexts:finance:finance-intercompany")
// include(":bounded-contexts:finance:finance-lease")
//
// // ... (Continue for all 92 modules)
// =============================================================================


// Platform-shared modules (cross-cutting concerns)
include("platform-shared:common-types")
include("platform-shared:common-messaging")
include("platform-shared:common-security")
include("platform-shared:common-observability")
include("platform-shared:config-model")
include("platform-shared:org-model")
include("platform-shared:workflow-model")

// Tenancy-Identity Bounded Context (ADR-005)
include("tenancy-identity:tenancy-shared")
include("tenancy-identity:tenancy-core")
include("tenancy-identity:identity-core")

// Finance Shared - Common types across all finance subdomains
include("finance:finance-shared")

// Bounded Contexts - Finance Domain
include("finance:finance-gl:gl-domain")
include("finance:finance-gl:gl-application")
include("finance:finance-gl:gl-infrastructure")

include("finance:finance-ar:ar-domain")
include("finance:finance-ar:ar-application")
include("finance:finance-ar:ar-infrastructure")

include("finance:finance-ap:ap-domain")
include("finance:finance-ap:ap-application")
include("finance:finance-ap:ap-infrastructure")

include("finance:finance-assets:assets-domain")
include("finance:finance-assets:assets-application")
include("finance:finance-assets:assets-infrastructure")

include("finance:finance-tax:tax-domain")
include("finance:finance-tax:tax-application")
include("finance:finance-tax:tax-infrastructure")

// Finance Public Sector Subdomain (ADR-050)
// MISSING: include("finance:finance-public-sector")
// MISSING: include("finance:finance-public-sector:finance-public-sector-domain")
// MISSING: include("finance:finance-public-sector:finance-public-sector-application")
// MISSING: include("finance:finance-public-sector:finance-public-sector-infrastructure")

// Insurance Bounded Context (ADR-051)
// MISSING: include("insurance:insurance-shared")
// MISSING: include("insurance:insurance-policy")
// MISSING: include("insurance:insurance-policy:insurance-policy-domain")
// MISSING: include("insurance:insurance-policy:insurance-policy-application")
// MISSING: include("insurance:insurance-policy:insurance-policy-infrastructure")

// MISSING: include("insurance:insurance-claims")
// MISSING: include("insurance:insurance-claims:insurance-claims-domain")
// MISSING: include("insurance:insurance-claims:insurance-claims-application")
// MISSING: include("insurance:insurance-claims:insurance-claims-infrastructure")

// MISSING: include("insurance:insurance-underwriting")
// MISSING: include("insurance:insurance-underwriting:insurance-underwriting-domain")
// MISSING: include("insurance:insurance-underwriting:insurance-underwriting-application")
// MISSING: include("insurance:insurance-underwriting:insurance-underwriting-infrastructure")

// MISSING: include("insurance:insurance-actuarial")
// MISSING: include("insurance:insurance-actuarial:insurance-actuarial-domain")
// MISSING: include("insurance:insurance-actuarial:insurance-actuarial-application")
// MISSING: include("insurance:insurance-actuarial:insurance-actuarial-infrastructure")

// MISSING: include("insurance:insurance-reinsurance")
// MISSING: include("insurance:insurance-reinsurance:insurance-reinsurance-domain")
// MISSING: include("insurance:insurance-reinsurance:insurance-reinsurance-application")
// MISSING: include("insurance:insurance-reinsurance:insurance-reinsurance-infrastructure")
