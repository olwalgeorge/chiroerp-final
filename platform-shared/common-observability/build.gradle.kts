// common-observability build configuration
// ADR-006: Technical abstractions only - NO business logic
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}