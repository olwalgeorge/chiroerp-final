// ap-application build configuration
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-ap:ap-domain"))
    implementation(project(":platform-shared:common-types"))
    implementation(project(":bounded-contexts:finance:finance-shared"))
    implementation("jakarta.transaction:jakarta.transaction-api:2.0.1")
}
