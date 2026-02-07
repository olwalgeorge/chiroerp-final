// tax-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-tax:tax-domain"))
    implementation(project(":bounded-contexts:finance:finance-tax:tax-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:finance:finance-shared"))
}