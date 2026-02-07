// ap-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-ap:ap-domain"))
    implementation(project(":bounded-contexts:finance:finance-ap:ap-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:finance:finance-shared"))
}