// identity-core-application build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":tenancy-identity:identity-core:identity-core-domain"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":tenancy-identity:tenancy-shared"))
}