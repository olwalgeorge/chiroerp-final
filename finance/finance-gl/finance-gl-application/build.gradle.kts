// finance-gl-application build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":finance:finance-gl:finance-gl-domain"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))
}
