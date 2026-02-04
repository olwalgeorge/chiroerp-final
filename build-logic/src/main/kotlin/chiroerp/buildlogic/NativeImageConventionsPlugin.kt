package chiroerp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

/**
 * ChiroERP Native Image Conventions Plugin
 *
 * Extends quarkus-conventions to configure GraalVM native image builds.
 * This plugin adds production-ready native executable configuration.
 *
 * Features:
 * - Native image optimization flags
 * - Security extensions (Elytron)
 * - OpenTelemetry for observability
 * - PostgreSQL native support
 * - Redis cache support
 * - Container-based build support
 *
 * Usage in module build.gradle.kts:
 *   plugins {
 *       id("chiroerp.native-image-conventions")
 *   }
 *
 * Build native executable:
 *   ./gradlew build -Dquarkus.package.type=native
 *
 * Build in container (no local GraalVM required):
 *   ./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
 */
class NativeImageConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        // Apply Quarkus conventions first (which includes kotlin-conventions)
        pluginManager.apply("chiroerp.quarkus-conventions")

        // Add native image specific dependencies
        dependencies {
            // Security (production-ready)
            "implementation"("io.quarkus:quarkus-security")
            "implementation"("io.quarkus:quarkus-elytron-security-jdbc")

            // Observability
            "implementation"("io.quarkus:quarkus-opentelemetry")
            "implementation"("io.quarkus:quarkus-micrometer")
            "implementation"("io.quarkus:quarkus-micrometer-registry-prometheus")

            // Database (native support)
            "implementation"("io.quarkus:quarkus-jdbc-postgresql")
            "implementation"("io.quarkus:quarkus-hibernate-orm")

            // Cache
            "implementation"("io.quarkus:quarkus-redis-client")
            "implementation"("io.quarkus:quarkus-cache")
        }

        // Configure native image properties
        // These can be overridden in application.properties or via system properties
        tasks.named("build") {
            doFirst {
                val isNativeBuild = System.getProperty("quarkus.package.type") == "native"
                if (isNativeBuild) {
                    println("🚀 Building NATIVE executable for ${project.name}")
                    println("   This may take 5-10 minutes...")
                    println("   Container build: ${System.getProperty("quarkus.native.container-build", "false")}")
                }
            }
        }
    }
}
