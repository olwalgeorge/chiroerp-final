// assets-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-assets:assets-domain"))
    implementation(project(":finance:finance-assets:assets-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}