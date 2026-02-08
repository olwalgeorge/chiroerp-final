plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-shared"))
    implementation(project(":bounded-contexts:finance:finance-assets:assets-domain"))
    implementation(project(":bounded-contexts:finance:finance-assets:assets-application"))

    implementation(project(":platform-shared:common-types"))
    implementation(project(":platform-shared:common-messaging"))
    implementation(project(":platform-shared:common-security"))
    implementation(project(":platform-shared:common-observability"))
}
