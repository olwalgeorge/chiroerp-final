// assets-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-assets:assets-domain"))
    implementation(project(":bounded-contexts:finance:finance-assets:assets-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:finance:finance-shared"))
}