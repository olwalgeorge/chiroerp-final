// gl-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-gl:gl-domain"))
    implementation(project(":finance:finance-gl:gl-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}