// ar-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-ar:ar-domain"))
    implementation(project(":bounded-contexts:finance:finance-ar:ar-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:finance:finance-shared"))
}