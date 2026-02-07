// ap-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-ap:ap-domain"))
    implementation(project(":finance:finance-ap:ap-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}