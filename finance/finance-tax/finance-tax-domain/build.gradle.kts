// finance-tax-domain build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
