// finance-tax build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))


}
