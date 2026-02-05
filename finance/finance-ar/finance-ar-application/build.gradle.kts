// finance-ar-application build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":finance:finance-ar:finance-ar-domain"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
