// finance-tax-application build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":finance:finance-tax:finance-tax-domain"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
