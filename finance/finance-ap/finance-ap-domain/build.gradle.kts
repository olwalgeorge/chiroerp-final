// finance-ap-domain build configuration
plugins {
    id("chiroerp.quarkus-conventions")
}

dependencies {
    implementation(project(":finance:finance-ap"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":finance:finance-shared"))


}
