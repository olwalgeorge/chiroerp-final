/*
 * ChiroERP - Root Build Configuration
 * Gradle 9.0 with Kotlin DSL
 *
 * This is the root build file that applies to all subprojects.
 * Individual module build scripts should be minimal (<20 lines) and
 * use convention plugins for shared configuration.
 */

import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.GradleException
import org.gradle.api.tasks.testing.Test
import java.io.File
import java.util.concurrent.TimeUnit

plugins {
    // Base plugins applied to all projects
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.quarkus) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    idea
}

val isWindowsHost = System.getProperty("os.name").lowercase().contains("win")
val nodeExecutable = if (isWindowsHost) "node.exe" else "node"
val npxExecutable = if (isWindowsHost) "npx.cmd" else "npx"
val redoclyCliPackage = "@redocly/cli@latest"

fun Project.usesQuarkusConventions(): Boolean {
    if (!buildFile.exists()) {
        return false
    }

    return buildFile.readText().contains("chiroerp.quarkus-conventions")
}

fun Project.hasOpenApiExportConfigured(): Boolean {
    val yml = file("src/main/resources/application.yml")
    val yaml = file("src/main/resources/application.yaml")
    val properties = file("src/main/resources/application.properties")

    val marker = "store-schema-directory"
    return listOf(yml, yaml, properties).any { file ->
        file.exists() && file.readText().contains(marker)
    }
}

fun Project.findOpenApiSpecFiles(): List<File> {
    return fileTree(rootDir) {
        include("**/build/openapi/openapi.yaml")
        include("**/build/openapi/openapi.yml")
        include("**/build/openapi/openapi.json")
    }.files.sortedBy { it.absolutePath }
}

fun runCommand(command: List<String>, workingDirectory: File, timeoutSeconds: Long = 0): Int {
    val process = ProcessBuilder(command)
        .directory(workingDirectory)
        .inheritIO()
        .start()
    return if (timeoutSeconds > 0) {
        if (process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.exitValue()
        } else {
            process.destroyForcibly()
            124
        }
    } else {
        process.waitFor()
    }
}

fun commandAvailable(command: List<String>, workingDirectory: File): Boolean {
    return try {
        val process = ProcessBuilder(command)
            .directory(workingDirectory)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
        process.waitFor(20, TimeUnit.SECONDS) && process.exitValue() == 0
    } catch (_: Exception) {
        false
    }
}

fun Project.redoclyBaseCommand(): List<String> {
    val localBinary = file("node_modules/.bin/${if (isWindowsHost) "redocly.cmd" else "redocly"}")
    val globalBinary = if (isWindowsHost) "redocly.cmd" else "redocly"
    val allowNpxFallback = (findProperty("allowNpxRedoclyFallback")?.toString()?.toBooleanStrictOrNull() == true)

    return when {
        localBinary.exists() -> {
            listOf(localBinary.absolutePath)
        }
        commandAvailable(listOf(globalBinary, "--version"), rootDir) -> {
            listOf(globalBinary)
        }
        allowNpxFallback -> {
            listOf(npxExecutable, "--yes", redoclyCliPackage)
        }
        else -> {
            throw GradleException(
                "Redocly CLI not found. Install with 'npm install -g @redocly/cli' " +
                    "or run with -PallowNpxRedoclyFallback=true."
            )
        }
    }
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
                "-progressive"               // Enable progressive mode
                // Note: Kotlin 2.0+ uses context parameters by default, removed -Xcontext-receivers
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
                project.path.contains(":bounded-contexts:tenancy-identity") -> "Tenancy & Identity Domain"
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
                project.path.contains(":bounded-contexts") -> "Bounded Contexts (Other)"
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

// =============================================================================
// ARCHITECTURE ENFORCEMENT TASKS
// =============================================================================

abstract class ArchitectureCheckTask : DefaultTask() {
    @get:Input
    val domainModules = mutableListOf<String>()

    @get:Input
    val platformModules = mutableListOf<String>()

    @get:Input
    val domainDependencies = mutableMapOf<String, List<String>>()

    @get:Input
    val platformDependencies = mutableMapOf<String, List<String>>()

    @TaskAction
    fun check() {
        println("\n=== Architecture Compliance Check ===\n")

        var violations = 0

        // Rule 1: Domain modules should not depend on infrastructure or frameworks
        domainDependencies.forEach { (module, deps) ->
            deps.forEach { dep ->
                if (dep.contains("quarkus") || dep.contains("spring")) {
                    println("❌ VIOLATION: Domain module $module depends on framework: $dep")
                    violations++
                }

                if (dep.endsWith("-infrastructure")) {
                    println("❌ VIOLATION: Domain module $module depends on infrastructure module $dep")
                    violations++
                }
            }
        }

        // Rule 2: platform-shared should not depend on bounded-contexts
        platformDependencies.forEach { (module, deps) ->
            deps.forEach { dep ->
                if (dep.contains("bounded-contexts")) {
                    println("❌ VIOLATION: Platform-shared module $module depends on bounded context/domain module")
                    violations++
                }
            }
        }

        if (violations == 0) {
            println("✓ All architectural rules satisfied")
        } else {
            println("\n⚠️  Found $violations architectural violation(s)")
            println("See ADR-001 (CQRS), ADR-002 (Database-per-Context), ADR-006 (Platform Governance)")
            throw GradleException("Architecture compliance failed with $violations violation(s)")
        }

        println("\n=====================================\n")
    }
}

tasks.register<ArchitectureCheckTask>("checkArchitecture") {
    group = "verification"
    description = "Validates architectural rules (ADR-001, ADR-002, ADR-006)"

    // Capture dependency data during configuration phase
    val domainProjects = subprojects.filter { it.name.endsWith("-domain") }
    val platformProjects = subprojects.filter { it.path.contains(":platform-shared") }

    domainModules.addAll(domainProjects.map { it.path })
    platformModules.addAll(platformProjects.map { it.path })

    domainProjects.forEach { domainProject ->
        val config = domainProject.configurations.findByName("implementation")
        val deps = config?.dependencies?.map { dep ->
            if (dep is ProjectDependency) dep.name else dep.name
        } ?: emptyList()
        domainDependencies[domainProject.path] = deps
    }

    platformProjects.forEach { platformProject ->
        val config = platformProject.configurations.findByName("implementation")
        val deps = config?.dependencies?.map { it.toString() } ?: emptyList()
        platformDependencies[platformProject.path] = deps
    }
}

val architectureTest = tasks.register("architectureTest") {
    group = "verification"
    description = "Runs the ADR architecture test suite (ArchUnit)"
    dependsOn(project(":architecture-tests").tasks.named("test"))
}

tasks.register("validateArchitecture") {
    group = "verification"
    description = "Runs all architecture quality gates (dependency rules + ArchUnit suite)"
    notCompatibleWithConfigurationCache("Includes checkArchitecture task that inspects the project model")
    dependsOn("checkArchitecture", architectureTest)
}

tasks.register("preCommit") {
    group = "verification"
    description = "Fast pre-commit checks for architecture compliance"
    dependsOn("validateArchitecture")
}

// =============================================================================
// OPENAPI & API GOVERNANCE TASKS
// =============================================================================

tasks.register("generateOpenApiSpecs") {
    group = "documentation"
    description = "Build OpenAPI-enabled modules and export specs into build/openapi"

    subprojects
        .filter { it.usesQuarkusConventions() && it.hasOpenApiExportConfigured() }
        .forEach { module ->
            dependsOn("${module.path}:quarkusBuild")
        }
}

tasks.register("generateApiDocs") {
    group = "documentation"
    description = "Generate static Redoc HTML documentation from OpenAPI specs"
    dependsOn("generateOpenApiSpecs")
    notCompatibleWithConfigurationCache("Executes external Node/Redocly processes at task runtime")

    doFirst {
        project.file("docs/api").mkdirs()

        val nodeCheck = runCommand(
            command = listOf(nodeExecutable, "--version"),
            workingDirectory = project.rootDir,
        )

        if (nodeCheck != 0) {
            throw GradleException("Node.js is required. Install from https://nodejs.org/")
        }
    }

    doLast {
        val specs = project.findOpenApiSpecFiles()
        if (specs.isEmpty()) {
            throw GradleException("No OpenAPI specs found under **/build/openapi/. Run './gradlew generateOpenApiSpecs' first.")
        }

        val redoclyCommand = project.redoclyBaseCommand()

        specs.forEach { spec ->
            val moduleName = spec.parentFile?.parentFile?.parentFile?.name ?: spec.nameWithoutExtension
            val outputFile = project.file("docs/api/$moduleName.html")

            logger.lifecycle("Generating API docs for {} from {}", moduleName, spec.invariantSeparatorsPath)
            val result = runCommand(
                redoclyCommand + listOf(
                    "build-docs",
                    spec.absolutePath,
                    "--output=${outputFile.absolutePath}",
                ),
                workingDirectory = project.rootDir,
                timeoutSeconds = 240,
            )

            if (result == 124) {
                throw GradleException("Timed out generating docs for ${spec.invariantSeparatorsPath}")
            }
            if (result != 0) {
                throw GradleException("Failed generating docs for ${spec.invariantSeparatorsPath}")
            }
        }

        logger.lifecycle("API documentation generated in docs/api/")
    }
}

tasks.register("lintApiSpecs") {
    group = "verification"
    description = "Lint OpenAPI specifications with Redocly CLI"
    dependsOn("generateOpenApiSpecs")
    notCompatibleWithConfigurationCache("Executes external Node/Redocly processes at task runtime")

    doFirst {
        val nodeCheck = runCommand(
            command = listOf(nodeExecutable, "--version"),
            workingDirectory = project.rootDir,
        )

        if (nodeCheck != 0) {
            throw GradleException("Node.js is required. Install from https://nodejs.org/")
        }

        if (!project.file(".redocly.yaml").exists()) {
            throw GradleException("Missing .redocly.yaml configuration file")
        }
    }

    doLast {
        val specs = project.findOpenApiSpecFiles()
        if (specs.isEmpty()) {
            throw GradleException("No OpenAPI specs found under **/build/openapi/. Run './gradlew generateOpenApiSpecs' first.")
        }

        logger.lifecycle("Linting {} OpenAPI specification(s)", specs.size)
        val result = runCommand(
            project.redoclyBaseCommand() + listOf("lint") + specs.map { it.absolutePath } + listOf("--format=stylish"),
            workingDirectory = project.rootDir,
            timeoutSeconds = 300,
        )

        if (result == 124) {
            throw GradleException("OpenAPI linting timed out. Install Redocly locally/global or reduce spec scope.")
        }
        if (result != 0) {
            throw GradleException("OpenAPI linting failed. See Redocly output above for details.")
        }
    }
}

tasks.register("apiGovernance") {
    group = "verification"
    description = "Run complete API governance workflow: generate specs + lint + generate docs"
    dependsOn("lintApiSpecs", "generateApiDocs")

    doLast {
        println(
            """

            ✅ API Governance Complete!

            📋 OpenAPI specs generated in: **/build/openapi/
            📚 Static docs generated in: docs/api/

            Next steps:
            - Open docs/api/*.html in browser to view API docs
            - Review linting output above
            - Commit changes if specs meet governance rules

            """.trimIndent(),
        )
    }
}

tasks.register("installGitHooks") {
    group = "verification"
    description = "Install git hooks to run ./gradlew preCommit"

    doLast {
        val hooksDir = file(".git/hooks")
        if (!hooksDir.exists()) {
            println("Warning: .git/hooks directory not found; are you in a git repository?")
            return@doLast
        }

        val hookFile = file(".git/hooks/pre-commit")
        hookFile.writeText(
            """#!/bin/sh
./gradlew preCommit -PchangedOnly
"""
        )
        hookFile.setExecutable(true)
        println("Installed git pre-commit hook -> ./gradlew preCommit -PchangedOnly")
    }
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "9.0"
    distributionType = Wrapper.DistributionType.ALL
}

// =============================================================================
// TOOL INSTALLATION TASKS
// =============================================================================

tasks.register("installRedocly") {
    group = "tools"
    description = "Install Redocly CLI locally in node_modules for API linting"
    notCompatibleWithConfigurationCache("Executes external npm commands at task runtime")

    doLast {
        val nodeCheck = runCommand(
            command = listOf(nodeExecutable, "--version"),
            workingDirectory = project.rootDir,
        )

        if (nodeCheck != 0) {
            throw GradleException("Node.js is required. Install from https://nodejs.org/")
        }

        // Initialize package.json if it doesn't exist
        val packageJson = project.file("package.json")
        if (!packageJson.exists()) {
            logger.lifecycle("Initializing package.json...")
            val initResult = runCommand(
                command = if (isWindowsHost) listOf("cmd", "/c", "npm", "init", "-y") else listOf("npm", "init", "-y"),
                workingDirectory = project.rootDir,
            )
            if (initResult != 0) {
                throw GradleException("Failed to initialize package.json")
            }
        }

        // Install Redocly CLI
        logger.lifecycle("Installing Redocly CLI...")
        val installResult = runCommand(
            command = if (isWindowsHost) listOf("cmd", "/c", "npm", "install", "--save-dev", "@redocly/cli@1.25.5") 
                      else listOf("npm", "install", "--save-dev", "@redocly/cli@1.25.5"),
            workingDirectory = project.rootDir,
        )

        if (installResult != 0) {
            throw GradleException("Failed to install Redocly CLI")
        }

        logger.lifecycle("✅ Redocly CLI installed successfully!")
        logger.lifecycle("   Location: node_modules/.bin/redocly")
        logger.lifecycle("   Run './gradlew lintApiSpecs' to lint OpenAPI specs")
    }
}

tasks.register("installAsyncApiCli") {
    group = "tools"
    description = "Install AsyncAPI CLI locally for event API validation"
    notCompatibleWithConfigurationCache("Executes external npm commands at task runtime")

    doLast {
        val nodeCheck = runCommand(
            command = listOf(nodeExecutable, "--version"),
            workingDirectory = project.rootDir,
        )

        if (nodeCheck != 0) {
            throw GradleException("Node.js is required. Install from https://nodejs.org/")
        }

        // Initialize package.json if it doesn't exist
        val packageJson = project.file("package.json")
        if (!packageJson.exists()) {
            logger.lifecycle("Initializing package.json...")
            val initResult = runCommand(
                command = if (isWindowsHost) listOf("cmd", "/c", "npm", "init", "-y") else listOf("npm", "init", "-y"),
                workingDirectory = project.rootDir,
            )
            if (initResult != 0) {
                throw GradleException("Failed to initialize package.json")
            }
        }

        // Install AsyncAPI CLI
        logger.lifecycle("Installing AsyncAPI CLI...")
        val installResult = runCommand(
            command = if (isWindowsHost) listOf("cmd", "/c", "npm", "install", "--save-dev", "@asyncapi/cli@2.3.0") 
                      else listOf("npm", "install", "--save-dev", "@asyncapi/cli@2.3.0"),
            workingDirectory = project.rootDir,
        )

        if (installResult != 0) {
            throw GradleException("Failed to install AsyncAPI CLI")
        }

        logger.lifecycle("✅ AsyncAPI CLI installed successfully!")
        logger.lifecycle("   Location: node_modules/.bin/asyncapi")
        logger.lifecycle("   Run './gradlew validateAsyncApiSpecs' to validate event specs")
    }
}

tasks.register("installApiTools") {
    group = "tools"
    description = "Install all API governance tools (Redocly + AsyncAPI CLI)"
    dependsOn("installRedocly", "installAsyncApiCli")

    doLast {
        logger.lifecycle("""
            
            ✅ All API tools installed!
            
            Available commands:
            - ./gradlew lintApiSpecs        # Lint OpenAPI specifications
            - ./gradlew generateApiDocs     # Generate OpenAPI documentation
            - ./gradlew validateAsyncApiSpecs # Validate AsyncAPI specifications
            - ./gradlew generateAsyncApiDocs  # Generate AsyncAPI documentation
            
        """.trimIndent())
    }
}

// =============================================================================
// ASYNCAPI & EVENT GOVERNANCE TASKS
// =============================================================================

fun Project.asyncApiCliBaseCommand(): List<String> {
    val localBinary = file("node_modules/.bin/${if (isWindowsHost) "asyncapi.cmd" else "asyncapi"}")
    val globalBinary = if (isWindowsHost) "asyncapi.cmd" else "asyncapi"
    val allowNpxFallback = (findProperty("allowNpxAsyncApiFallback")?.toString()?.toBooleanStrictOrNull() == true)

    return when {
        localBinary.exists() -> {
            listOf(localBinary.absolutePath)
        }
        commandAvailable(listOf(globalBinary, "--version"), rootDir) -> {
            listOf(globalBinary)
        }
        allowNpxFallback -> {
            listOf(npxExecutable, "--yes", "@asyncapi/cli@2.3.0")
        }
        else -> {
            throw GradleException(
                "AsyncAPI CLI not found. Install with './gradlew installAsyncApiCli', " +
                    "'npm install -g @asyncapi/cli', or run with -PallowNpxAsyncApiFallback=true."
            )
        }
    }
}

fun Project.findAsyncApiSpecFiles(): List<File> {
    return fileTree(file("config/asyncapi")) {
        include("*.yaml")
        include("*.yml")
    }.files.sortedBy { it.name }
}

tasks.register("validateAsyncApiSpecs") {
    group = "verification"
    description = "Validate AsyncAPI specifications for event-driven APIs"
    notCompatibleWithConfigurationCache("Executes external Node/AsyncAPI CLI processes at task runtime")

    doFirst {
        val nodeCheck = runCommand(
            command = listOf(nodeExecutable, "--version"),
            workingDirectory = project.rootDir,
        )

        if (nodeCheck != 0) {
            throw GradleException("Node.js is required. Install from https://nodejs.org/")
        }
    }

    doLast {
        val specs = project.findAsyncApiSpecFiles()
        if (specs.isEmpty()) {
            logger.lifecycle("No AsyncAPI specs found in config/asyncapi/")
            return@doLast
        }

        val asyncApiCommand = project.asyncApiCliBaseCommand()
        var errors = 0

        specs.forEach { spec ->
            logger.lifecycle("Validating AsyncAPI spec: {}", spec.name)
            val result = runCommand(
                asyncApiCommand + listOf("validate", spec.absolutePath, "--fail-severity=error"),
                workingDirectory = project.rootDir,
                timeoutSeconds = 120,
            )

            if (result == 124) {
                throw GradleException("Timed out validating ${spec.name}")
            }
            if (result != 0) {
                logger.error("❌ Validation failed: {}", spec.name)
                errors++
            } else {
                logger.lifecycle("✅ Valid: {}", spec.name)
            }
        }

        if (errors > 0) {
            throw GradleException("AsyncAPI validation failed for $errors spec(s)")
        }

        logger.lifecycle("✅ All {} AsyncAPI specs are valid!", specs.size)
    }
}

tasks.register("generateAsyncApiDocs") {
    group = "documentation"
    description = "Generate HTML documentation from AsyncAPI specifications"
    dependsOn("validateAsyncApiSpecs")
    notCompatibleWithConfigurationCache("Executes external Node/AsyncAPI CLI processes at task runtime")

    doLast {
        val specs = project.findAsyncApiSpecFiles()
        if (specs.isEmpty()) {
            logger.lifecycle("No AsyncAPI specs found in config/asyncapi/")
            return@doLast
        }

        project.file("docs/events").mkdirs()
        val asyncApiCommand = project.asyncApiCliBaseCommand()

        specs.forEach { spec ->
            val baseName = spec.nameWithoutExtension.removePrefix("asyncapi-")
            val outputDir = project.file("docs/events/$baseName")

            logger.lifecycle("Generating event docs for {} from {}", baseName, spec.name)
            val result = runCommand(
                asyncApiCommand + listOf(
                    "generate", "fromTemplate",
                    spec.absolutePath,
                    "@asyncapi/html-template",
                    "-o", outputDir.absolutePath,
                    "--force-write"
                ),
                workingDirectory = project.rootDir,
                timeoutSeconds = 240,
            )

            if (result == 124) {
                throw GradleException("Timed out generating docs for ${spec.name}")
            }
            if (result != 0) {
                throw GradleException("Failed generating docs for ${spec.name}")
            }
        }

        logger.lifecycle("✅ AsyncAPI documentation generated in docs/events/")
    }
}

tasks.register("eventGovernance") {
    group = "verification"
    description = "Run complete event API governance workflow: validate + generate docs"
    dependsOn("validateAsyncApiSpecs", "generateAsyncApiDocs")

    doLast {
        println(
            """

            ✅ Event API Governance Complete!

            📋 AsyncAPI specs validated in: config/asyncapi/
            📚 Event docs generated in: docs/events/

            Next steps:
            - Open docs/events/*/index.html in browser to view event docs
            - Review validation output above
            - Commit changes if specs meet governance rules

            """.trimIndent(),
        )
    }
}

tasks.register("allApiGovernance") {
    group = "verification"
    description = "Run complete API governance for both REST (OpenAPI) and events (AsyncAPI)"
    dependsOn("apiGovernance", "eventGovernance")

    doLast {
        println(
            """

            ✅ Complete API Governance Passed!

            REST APIs:
            - OpenAPI specs: **/build/openapi/
            - REST docs: docs/api/

            Event APIs:
            - AsyncAPI specs: config/asyncapi/
            - Event docs: docs/events/

            """.trimIndent(),
        )
    }
}
