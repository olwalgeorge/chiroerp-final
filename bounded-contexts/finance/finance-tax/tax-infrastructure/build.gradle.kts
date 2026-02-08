plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-shared"))
    implementation(project(":bounded-contexts:finance:finance-tax:tax-domain"))
    implementation(project(":bounded-contexts:finance:finance-tax:tax-application"))

    implementation(project(":platform-shared:common-types"))
    implementation(project(":platform-shared:common-messaging"))
    implementation(project(":platform-shared:common-security"))
    implementation(project(":platform-shared:common-observability"))
}
