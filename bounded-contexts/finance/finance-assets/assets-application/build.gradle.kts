// assets-application build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-assets:assets-domain"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:finance:finance-shared"))
}