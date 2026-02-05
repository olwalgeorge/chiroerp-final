// finance-ar-domain build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-ar"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))


}
