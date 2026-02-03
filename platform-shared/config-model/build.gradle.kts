plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    // Only depend on common-types (shared value objects)
    implementation(project(":platform-shared:common-types"))
    
    // No other dependencies - pure interfaces and value objects
    // Implementations will be in domain modules (hardcoded) or platform-infrastructure (config-driven)
}

tasks.test {
    useJUnitPlatform()
}
