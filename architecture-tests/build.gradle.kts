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
    testRuntimeOnly(project(":finance:finance-ap:finance-ap-domain"))
    testRuntimeOnly(project(":finance:finance-ap:finance-ap-application"))
    testRuntimeOnly(project(":finance:finance-ar:finance-ar-domain"))
    testRuntimeOnly(project(":finance:finance-ar:finance-ar-application"))
    testRuntimeOnly(project(":finance:finance-assets:finance-assets-domain"))
    testRuntimeOnly(project(":finance:finance-assets:finance-assets-application"))
    testRuntimeOnly(project(":finance:finance-gl:finance-gl-domain"))
    testRuntimeOnly(project(":finance:finance-gl:finance-gl-application"))
    testRuntimeOnly(project(":finance:finance-tax:finance-tax-domain"))
    testRuntimeOnly(project(":finance:finance-tax:finance-tax-application"))
}
