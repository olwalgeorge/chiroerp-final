// identity-core-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":tenancy-identity:identity-core:identity-core-domain"))
    implementation(project(":tenancy-identity:identity-core:identity-core-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":tenancy-identity:tenancy-shared"))
}