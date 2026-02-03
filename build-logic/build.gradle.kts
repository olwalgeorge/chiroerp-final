plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Gradle Kotlin DSL
    implementation(kotlin("gradle-plugin", "2.2.0"))
    implementation(kotlin("allopen", "2.2.0"))
    implementation(kotlin("noarg", "2.2.0"))
    implementation("io.quarkus:gradle-application-plugin:3.31.1")
}

gradlePlugin {
    plugins {
        create("kotlin-conventions") {
            id = "chiroerp.kotlin-conventions"
            implementationClass = "chiroerp.buildlogic.KotlinConventionsPlugin"
        }
        create("quarkus-conventions") {
            id = "chiroerp.quarkus-conventions"
            implementationClass = "chiroerp.buildlogic.QuarkusConventionsPlugin"
        }
        create("native-image-conventions") {
            id = "chiroerp.native-image-conventions"
            implementationClass = "chiroerp.buildlogic.NativeImageConventionsPlugin"
        }
    }
}
