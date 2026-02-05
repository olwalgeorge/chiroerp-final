// finance-ap-application build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":finance:finance-ap:finance-ap-domain"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
