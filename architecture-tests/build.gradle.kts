plugins {
    id("chiroerp.kotlin-conventions")
}

tasks.withType<Test>().configureEach {
    // Override the global log manager to avoid JBoss LogManager dependency for arch tests.
    systemProperty("java.util.logging.manager", "java.util.logging.LogManager")
}

dependencies {
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")

    // Test against the main modules
    testRuntimeOnly(project(":platform-shared:common-types"))
    testRuntimeOnly(project(":finance:finance-shared"))
    testRuntimeOnly(project(":finance:finance-ap:ap-domain"))
    testRuntimeOnly(project(":finance:finance-ap:ap-application"))
    testRuntimeOnly(project(":finance:finance-ar:ar-domain"))
    testRuntimeOnly(project(":finance:finance-ar:ar-application"))
    testRuntimeOnly(project(":finance:finance-assets:assets-domain"))
    testRuntimeOnly(project(":finance:finance-assets:assets-application"))
    testRuntimeOnly(project(":finance:finance-gl:gl-domain"))
    testRuntimeOnly(project(":finance:finance-gl:gl-application"))
    testRuntimeOnly(project(":finance:finance-tax:tax-domain"))
    testRuntimeOnly(project(":finance:finance-tax:tax-application"))
}
