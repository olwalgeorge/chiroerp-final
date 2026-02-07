// tenancy-shared build configuration
// ADR-005/ADR-006: Context-specific value objects for Tenancy & Identity
// Contains: TenantId, TenantContext, Email, PhoneNumber, Address
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}