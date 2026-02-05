package chiroerp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

/**
 * ChiroERP Quarkus Conventions Plugin
 *
 * Applies Quarkus framework configuration for microservices.
 * Inherits from kotlin-conventions and adds Quarkus-specific setup.
 *
 * Features:
 * - Quarkus 3.29.0 application plugin
 * - Quarkus BOM for dependency management
 * - Core Quarkus extensions (REST, Jackson, Hibernate Validator)
 * - Quarkus testing framework
 * - Dev mode support (./gradlew quarkusDev)
 *
 * Usage in module build.gradle.kts:
 *   plugins {
 *       id("chiroerp.quarkus-conventions")
 *   }
 *
 *   dependencies {
 *       // Quarkus dependencies are pre-configured
 *       // Add module-specific dependencies here
 *       implementation(projects.platformShared.commonTypes)
 *   }
 */
class QuarkusConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        // Apply base Kotlin conventions first
        pluginManager.apply("chiroerp.kotlin-conventions")

        // Apply Quarkus plugin
        pluginManager.apply("io.quarkus")

        // Apply Kotlin plugins for JPA/AllOpen (needed for Hibernate entities)
        pluginManager.apply("org.jetbrains.kotlin.plugin.allopen")
        pluginManager.apply("org.jetbrains.kotlin.plugin.noarg")

        // AllOpen will be configured in the module's build file via allOpen {} block

        // Add Quarkus dependencies
        dependencies {
            // Quarkus BOM (Bill of Materials) for version management
            "implementation"(platform("io.quarkus.platform:quarkus-bom:3.31.1"))

            // Core Quarkus extensions
            "implementation"("io.quarkus:quarkus-arc")                    // CDI/DI
            "implementation"("io.quarkus:quarkus-rest")                   // REST endpoints
            "implementation"("io.quarkus:quarkus-rest-jackson")          // JSON serialization
            "implementation"("io.quarkus:quarkus-hibernate-validator")   // Bean validation

            // Database extensions
            "implementation"("io.quarkus:quarkus-jdbc-postgresql")       // PostgreSQL JDBC driver
            "implementation"("io.quarkus:quarkus-hibernate-orm")         // Hibernate ORM
            "implementation"("io.quarkus:quarkus-hibernate-orm-panache") // Panache for simplified ORM

            // Messaging extensions (optional - add as needed)
            // "implementation"("io.quarkus:quarkus-smallrye-reactive-messaging") // Reactive messaging
            // "implementation"("io.quarkus:quarkus-kafka-client")               // Kafka client

            // Kotlin support
            "implementation"("io.quarkus:quarkus-kotlin")
            "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

            // Logging
            "implementation"("io.quarkus:quarkus-logging-json")

            // Quarkus testing
            "testImplementation"("io.quarkus:quarkus-junit5")
            "testImplementation"("io.rest-assured:rest-assured:5.5.0")
        }

        // Custom tasks for Quarkus
        tasks.register("runDev") {
            group = "quarkus"
            description = "Run Quarkus in dev mode with live reload"
            dependsOn("quarkusDev")
        }
    }
}
