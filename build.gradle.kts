/*
 * ChiroERP - Root Build Configuration
 * Gradle 9.0 with Kotlin DSL
 *
 * This is the root build file that applies to all subprojects.
 * Individual module build scripts should be minimal (<20 lines) and
 * use convention plugins for shared configuration.
 */

plugins {
    // Base plugins applied to all projects
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.quarkus) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    idea
}

// =============================================================================
// GLOBAL PROJECT CONFIGURATION
// =============================================================================

allprojects {
    group = "com.chiroerp"
    version = "1.0.0-SNAPSHOT"
    
    // Repositories are configured centrally in settings.gradle.kts
    // See dependencyResolutionManagement block
}

// =============================================================================
// SUBPROJECT CONFIGURATION
// =============================================================================

subprojects {
    // Apply common configuration to all subprojects

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",           // Strict null-safety
                "-progressive",              // Enable progressive mode
                "-Xcontext-receivers"        // Enable context receivers
            )
        }
    }
    
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        
        // Parallel test execution
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        
        // Test logging
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStackTraces = true
            showCauses = true
        }
        
        // JVM arguments for tests
        jvmArgs(
            "-Xmx2g",
            "-XX:+HeapDumpOnOutOfMemoryError"
        )
    }
    
    // TODO: Add ktlint and detekt configuration
    // See ADR-017 for code quality standards
}

// =============================================================================
// ROOT PROJECT TASKS
// =============================================================================

tasks.register<Delete>("clean") {
    group = "build"
    description = "Cleans all project build directories"
    
    delete(layout.buildDirectory)
    subprojects.forEach { subproject ->
        delete(subproject.layout.buildDirectory)
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds all ChiroERP modules"
    
    dependsOn(subprojects
        .filter { it.tasks.findByName("build") != null }
        .map { it.tasks.named("build") }
    )
}

tasks.register("testAll") {
    group = "verification"
    description = "Runs tests for all ChiroERP modules"
    
    dependsOn(subprojects.mapNotNull { 
        try { it.tasks.named("test") } catch (e: Exception) { null }
    })
}

tasks.register("listModules") {
    group = "help"
    description = "Lists all ChiroERP modules organized by domain"
    
    doLast {
        println("\n=== ChiroERP Module Structure ===\n")
        
        val modulesByDomain = subprojects.groupBy { project ->
            when {
                project.path.contains(":bounded-contexts:finance") -> "Finance Domain"
                project.path.contains(":bounded-contexts:inventory") -> "Inventory Domain"
                project.path.contains(":bounded-contexts:sales") -> "Sales Domain"
                project.path.contains(":bounded-contexts:procurement") -> "Procurement Domain"
                project.path.contains(":bounded-contexts:manufacturing") -> "Manufacturing Domain"
                project.path.contains(":bounded-contexts:quality") -> "Quality Domain"
                project.path.contains(":bounded-contexts:maintenance") -> "Maintenance Domain"
                project.path.contains(":bounded-contexts:crm") -> "CRM Domain"
                project.path.contains(":bounded-contexts:mdm") -> "Master Data Domain"
                project.path.contains(":bounded-contexts:analytics") -> "Analytics Domain"
                project.path.contains(":bounded-contexts:hcm") -> "HCM Domain"
                project.path.contains(":bounded-contexts:fleet") -> "Fleet Domain"
                project.path.contains(":platform-shared") -> "Platform Shared"
                project.path.contains(":platform-infrastructure") -> "Platform Infrastructure"
                project.path.contains(":api-gateway") -> "API Gateway"
                project.path.contains(":portal") -> "Portal/UI"
                else -> "Other"
            }
        }
        
        modulesByDomain.toSortedMap().forEach { (domain, projects) ->
            println("📦 $domain (${projects.size} modules)")
            projects.sortedBy { it.path }.forEach { project ->
                println("   └─ ${project.path}")
            }
            println()
        }
        
        println("Total Modules: ${subprojects.size}")
        println("=================================\n")
    }
}

tasks.register("checkArchitecture") {
    group = "verification"
    description = "Validates architectural rules (ADR-001, ADR-002, ADR-006)"
    
    doLast {
        println("\n=== Architecture Compliance Check ===\n")
        
        var violations = 0
        
        // Rule 1: Domain modules should not depend on infrastructure
        subprojects.filter { it.path.contains(":domain") }.forEach { domainProject ->
            val config = domainProject.configurations.findByName("implementation")
            config?.dependencies?.forEach { dep ->
                if (dep.name.contains("quarkus") || dep.name.contains("spring")) {
                    println("❌ VIOLATION: Domain module ${domainProject.path} depends on framework: ${dep.name}")
                    violations++
                }
            }
        }
        
        // Rule 2: platform-shared should not depend on bounded-contexts
        subprojects.filter { it.path.contains(":platform-shared") }.forEach { sharedProject ->
            val config = sharedProject.configurations.findByName("implementation")
            config?.dependencies?.forEach { dep ->
                if (dep.toString().contains("bounded-contexts")) {
                    println("❌ VIOLATION: Platform-shared module ${sharedProject.path} depends on bounded context")
                    violations++
                }
            }
        }
        
        if (violations == 0) {
            println("✓ All architectural rules satisfied")
        } else {
            println("\n⚠️  Found $violations architectural violation(s)")
            println("See ADR-001 (CQRS), ADR-002 (Database-per-Context), ADR-006 (Platform Governance)")
        }
        
        println("\n=====================================\n")
    }
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "9.0"
    distributionType = Wrapper.DistributionType.ALL
}
