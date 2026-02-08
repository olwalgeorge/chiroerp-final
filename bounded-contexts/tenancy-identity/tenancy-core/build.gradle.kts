plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:tenancy-identity:tenancy-shared"))

    implementation(project(":platform-shared:common-types"))
    implementation(project(":platform-shared:common-messaging"))
    implementation(project(":platform-shared:common-security"))
    implementation(project(":platform-shared:common-observability"))
}
