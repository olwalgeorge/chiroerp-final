// finance-assets-application build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":finance:finance-assets:finance-assets-domain"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
