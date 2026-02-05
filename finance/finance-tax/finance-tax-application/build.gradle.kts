// finance-tax-application build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-tax"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))


}
