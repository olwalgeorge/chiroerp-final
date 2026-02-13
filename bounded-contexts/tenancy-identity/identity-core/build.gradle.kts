plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:tenancy-identity:tenancy-shared"))

    implementation(project(":platform-shared:common-types"))
    implementation(project(":platform-shared:common-messaging"))
    implementation(project(":platform-shared:common-security"))
    implementation(project(":platform-shared:common-observability"))

    // Runtime infrastructure for identity bootstrap with tenant-scoped persistence.
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-redis-client")
    implementation(libs.kafka.clients)
    implementation(libs.bcrypt)

    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.kafka)
}
