// finance-assets-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-assets:finance-assets-domain"))
    implementation(project(":finance:finance-assets:finance-assets-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
