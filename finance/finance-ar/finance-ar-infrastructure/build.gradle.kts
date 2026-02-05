// finance-ar-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-ar:finance-ar-domain"))
    implementation(project(":finance:finance-ar:finance-ar-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
