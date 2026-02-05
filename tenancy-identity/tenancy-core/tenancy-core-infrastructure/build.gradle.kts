// tenancy-core-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":tenancy-identity:tenancy-core:tenancy-core-domain"))
    implementation(project(":tenancy-identity:tenancy-core:tenancy-core-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":tenancy-identity:tenancy-shared"))
}