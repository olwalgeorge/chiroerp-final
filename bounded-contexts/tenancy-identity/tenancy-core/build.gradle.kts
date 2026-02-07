// tenancy-core build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:tenancy-identity:tenancy-shared"))
}