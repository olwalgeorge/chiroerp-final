plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:tenancy-identity:tenancy-shared"))

    implementation(project(":platform-shared:common-types"))
    implementation(project(":platform-shared:common-messaging"))
    implementation(project(":platform-shared:common-security"))
    implementation(project(":platform-shared:common-observability"))

    // Runtime infrastructure for schema-based multi-tenancy module bootstrap.
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("org.apache.kafka:kafka-clients:3.8.0")

    // Enables @TestSecurity for controller-level security integration tests.
    testImplementation("io.quarkus:quarkus-test-security")
}
