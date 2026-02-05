package chiroerp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * ChiroERP Kotlin Conventions Plugin
 *
 * Applies base Kotlin/JVM configuration to all modules.
 * This is the foundation layer inherited by quarkus-conventions and native-image-conventions.
 *
 * Features:
 * - Kotlin JVM plugin configuration
 * - Java 21 toolchain
 * - Progressive Kotlin mode
 * - Strict null-safety (JSR-305)
 * - JUnit 5 + MockK + Kotest testing stack
 * - Parallel test execution
 *
 * Usage in module build.gradle.kts:
 *   plugins {
 *       id("chiroerp.kotlin-conventions")
 *   }
 */
class KotlinConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        // Apply Kotlin JVM plugin
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        // Configure Kotlin compilation
        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
                allWarningsAsErrors.set(false)

                // Enable progressive mode (opt-in to upcoming language features)
                freeCompilerArgs.addAll(
                    "-progressive",
                    "-Xjsr305=strict",        // Strict null-safety checks
                    "-Xcontext-parameters"    // Enable context parameters (new syntax)
                    // Note: -Xopt-in=kotlin.RequiresOptIn moved to @OptIn at usage site
                )

                // Enable explicit API mode for libraries (uncomment when ready)
                // if (project.path.contains("platform-shared")) {
                //     freeCompilerArgs.add("-Xexplicit-api=strict")
                // }
            }
            
            // Output Kotlin classes to java/main directory for Quarkus compatibility
            destinationDirectory.set(layout.buildDirectory.dir("classes/java/main"))
        }

        // Java toolchain configuration (Java 21)
        extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
            toolchain {
                languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(21))
                vendor.set(org.gradle.jvm.toolchain.JvmVendorSpec.ADOPTIUM)
            }
        }

        // Test configuration
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()

            // Parallel test execution
            maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

            // Test logging
            testLogging {
                events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
                exceptionFormat = TestExceptionFormat.FULL
                showStackTraces = true
                showCauses = true
            }

            // JVM arguments
            jvmArgs(
                "-Xmx2g",
                "-XX:+HeapDumpOnOutOfMemoryError",
                "-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
            )
        }

        // Add common test dependencies
        dependencies {
            // Kotlin
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
            "implementation"("org.jetbrains.kotlin:kotlin-reflect:2.2.0")

            // Testing frameworks
            "testImplementation"("org.junit.jupiter:junit-jupiter:5.11.2")
            "testImplementation"("io.mockk:mockk:1.13.12")
            "testImplementation"("io.kotest:kotest-runner-junit5:5.9.1")
            "testImplementation"("io.kotest:kotest-assertions-core:5.9.1")
            "testImplementation"("org.assertj:assertj-core:3.26.3")

            // Test runtime
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher:1.11.2")
            "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.11.2")
        }

        // Code quality tasks
        tasks.register("format") {
            group = "verification"
            description = "Format Kotlin code (placeholder - integrate ktlint/detekt)"
            doLast {
                println("✓ Code formatting (integrate ktlint plugin for real formatting)")
            }
        }

        // Handle duplicate kotlin_module files in JAR
        tasks.withType<Jar>().configureEach {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
}
