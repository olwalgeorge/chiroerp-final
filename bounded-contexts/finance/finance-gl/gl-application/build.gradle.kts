plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":bounded-contexts:finance:finance-shared"))
    implementation(project(":bounded-contexts:finance:finance-gl:gl-domain"))

    compileOnly("jakarta.validation:jakarta.validation-api:3.1.1")
}
