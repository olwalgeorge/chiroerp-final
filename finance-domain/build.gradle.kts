plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    // Platform Shared - Interfaces
    implementation(project(":platform-shared:common-types"))
    implementation(project(":platform-shared:common-messaging"))
    implementation(project(":platform-shared:config-model"))
    implementation(project(":platform-shared:org-model"))
    implementation(project(":platform-shared:workflow-model"))

    // REST API (quarkus-conventions provides core, REST, Jackson, Kotlin)
    implementation("io.quarkus:quarkus-smallrye-openapi")

    // Database - Hibernate ORM with Panache
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")

    // Validation
    implementation("io.quarkus:quarkus-hibernate-validator")

    // Observability
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-opentelemetry")

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.9.0")

    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.rest-assured:kotlin-extensions")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}
