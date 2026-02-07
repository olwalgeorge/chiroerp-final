// tax-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-tax:tax-domain"))
    implementation(project(":finance:finance-tax:tax-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}