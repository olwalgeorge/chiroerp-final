// ar-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-ar:ar-domain"))
    implementation(project(":finance:finance-ar:ar-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}