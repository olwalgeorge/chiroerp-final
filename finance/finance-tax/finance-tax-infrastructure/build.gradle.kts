// finance-tax-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-tax:finance-tax-domain"))
    implementation(project(":finance:finance-tax:finance-tax-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
