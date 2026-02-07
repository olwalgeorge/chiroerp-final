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
    testRuntimeOnly(project(":bounded-contexts:finance:finance-shared"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-ap:ap-domain"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-ap:ap-application"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-ar:ar-domain"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-ar:ar-application"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-assets:assets-domain"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-assets:assets-application"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-gl:gl-domain"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-gl:gl-application"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-tax:tax-domain"))
    testRuntimeOnly(project(":bounded-contexts:finance:finance-tax:tax-application"))
}
