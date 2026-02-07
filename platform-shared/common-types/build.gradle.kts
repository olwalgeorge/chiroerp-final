// common-types build configuration
// ADR-006: Shared kernel primitives - NO business logic
// FORBIDDEN: Money, Address, PhoneNumber, Email, TaxId (must be duplicated per context)
plugins {
    id("chiroerp.kotlin-conventions")
}

dependencies {
    // No dependencies - shared kernel primitives
}