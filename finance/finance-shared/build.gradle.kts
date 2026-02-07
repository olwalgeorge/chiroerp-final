// finance-shared build configuration
// ADR-006: Context-specific value objects for Finance bounded context
// Contains: Money (finance precision), Currency, TaxId, AccountNumber
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    implementation(project(":platform-shared:common-types"))
}