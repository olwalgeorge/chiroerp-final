// gl-infrastructure build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-gl:gl-domain"))
    implementation(project(":bounded-contexts:finance:finance-gl:gl-application"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:finance:finance-shared"))
}