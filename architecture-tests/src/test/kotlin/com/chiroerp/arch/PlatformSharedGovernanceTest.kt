package com.chiroerp.arch

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * ADR-006: Platform-Shared Governance Rules
 *
 * These tests enforce the governance rules defined in ADR-006 to prevent
 * the distributed monolith anti-pattern and maintain bounded context autonomy.
 *
 * Key Rules:
 * 1. platform-shared contains ONLY technical primitives and abstractions
 * 2. Business value objects (Money, Address, PhoneNumber, Email) must be
 *    duplicated in each bounded context's *-shared module
 * 3. No domain models, business logic, or shared DTOs in platform-shared
 * 4. platform-shared must not depend on any bounded context
 * 5. Bounded contexts must not depend on each other directly
 *
 * @see docs/adr/ADR-006-platform-shared-governance.md
 */
@DisplayName("ADR-006: Platform-Shared Governance Rules")
class PlatformSharedGovernanceTest {

    private val importedClasses = ClassFileImporter()
        .withImportOption { location ->
            !location.contains("/build/")
        }
        .importPackages("com.chiroerp")

    // =========================================================================
    // FORBIDDEN CONTENT IN PLATFORM-SHARED
    // =========================================================================

    @Nested
    @DisplayName("Forbidden Content in platform-shared")
    inner class ForbiddenContent {

        @Test
        @DisplayName("platform-shared must not contain domain models (Entity, Aggregate, Repository)")
        fun `platform-shared must not contain domain models`() {
            val rule = noClasses()
                .that().resideInAPackage("com.chiroerp.shared..")
                .should().haveSimpleNameEndingWith("Entity")
                .orShould().haveSimpleNameEndingWith("Aggregate")
                .orShould().haveSimpleNameEndingWith("Repository")
                .orShould().haveSimpleNameContaining("Calculator")
                .orShould().haveSimpleNameContaining("Policy")
                .orShould().haveSimpleNameContaining("Validator")
                .allowEmptyShould(true)
                .because("ADR-006: Domain models belong in bounded contexts, not platform-shared")

            rule.check(importedClasses)
        }

        @Test
        @DisplayName("platform-shared must not contain business value objects (Money, Address, PhoneNumber, Email)")
        fun `platform-shared must not contain forbidden business value objects`() {
            val rule = noClasses()
                .that().resideInAPackage("com.chiroerp.shared..")
                .should().haveSimpleNameContaining("Money")
                .orShould().haveSimpleNameContaining("Address")
                .orShould().haveSimpleNameContaining("PhoneNumber")
                .orShould().haveSimpleNameContaining("Email")
                .orShould().haveSimpleNameContaining("TaxId")
                .allowEmptyShould(true)
                .because("ADR-006: Business value objects must be duplicated per bounded context")

            rule.check(importedClasses)
        }

        @Test
        @DisplayName("platform-shared must not contain shared DTOs (Request/Response)")
        fun `platform-shared must not contain shared DTOs`() {
            val rule = noClasses()
                .that().resideInAPackage("com.chiroerp.shared..")
                .should().haveSimpleNameEndingWith("Request")
                .orShould().haveSimpleNameEndingWith("Response")
                .orShould().haveSimpleNameEndingWith("DTO")
                .orShould().haveSimpleNameEndingWith("Dto")
                .allowEmptyShould(true)
                .because("ADR-006: API contracts are context-specific")

            rule.check(importedClasses)
        }

        @Test
        @DisplayName("platform-shared must not contain utility classes")
        fun `platform-shared must not contain utility classes`() {
            val rule = noClasses()
                .that().resideInAPackage("com.chiroerp.shared..")
                .should().haveSimpleNameEndingWith("Utils")
                .orShould().haveSimpleNameEndingWith("Util")
                .orShould().haveSimpleNameEndingWith("Helper")
                .orShould().haveSimpleNameEndingWith("Helpers")
                .allowEmptyShould(true)
                .because("ADR-006: Avoid 'utils' dumping ground")

            rule.check(importedClasses)
        }

        @Test
        @DisplayName("platform-shared must not contain Service implementations")
        fun `platform-shared must not contain service implementations`() {
            val rule = noClasses()
                .that().resideInAPackage("com.chiroerp.shared..")
                .and().haveSimpleNameEndingWith("Service")
                .should(notBeConcreteImplementation())
                .allowEmptyShould(true)
                .because("ADR-006: Only interfaces allowed, implementations belong in bounded contexts")

            rule.check(importedClasses)
        }
    }

    // =========================================================================
    // DEPENDENCY RULES
    // =========================================================================

    @Nested
    @DisplayName("Dependency Rules")
    inner class DependencyRules {

        @Test
        @DisplayName("platform-shared must not depend on any bounded context")
        fun `platform-shared must not depend on bounded contexts`() {
            val rule = noClasses()
                .that().resideInAPackage("com.chiroerp.shared..")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "com.chiroerp.tenancy..",
                    "com.chiroerp.identity..",
                    "com.chiroerp.finance..",
                    "com.chiroerp.inventory..",
                    "com.chiroerp.commerce..",
                    "com.chiroerp.customer..",
                    "com.chiroerp.procurement..",
                    "com.chiroerp.manufacturing..",
                    "com.chiroerp.operations..",
                    "com.chiroerp.hr..",
                    "com.chiroerp.bi.."
                )
                .allowEmptyShould(true)
                .because("ADR-006: platform-shared is a dependency of contexts, not the reverse")

            rule.check(importedClasses)
        }

        @Test
        @DisplayName("Finance context must not depend on Inventory context directly")
        fun `finance must not depend on inventory directly`() {
            val rule = noClasses()
                .that().resideInAPackage("com.chiroerp.finance..")
                .should().dependOnClassesThat().resideInAPackage("com.chiroerp.inventory..")
                .allowEmptyShould(true)
                .because("ADR-006: Bounded contexts communicate via events, not direct dependencies")

            rule.check(importedClasses)
        }

        @Test
        @DisplayName("Inventory context must not depend on Finance context directly")
        fun `inventory must not depend on finance directly`() {
            val rule = noClasses()
                .that().resideInAPackage("com.chiroerp.inventory..")
                .should().dependOnClassesThat().resideInAPackage("com.chiroerp.finance..")
                .allowEmptyShould(true)
                .because("ADR-006: Bounded contexts communicate via events, not direct dependencies")

            rule.check(importedClasses)
        }

        @Test
        @DisplayName("Bounded contexts should only depend on platform-shared for shared types")
        fun `bounded contexts should depend on platform-shared for shared types`() {
            val rule = classes()
                .that().resideInAnyPackage(
                    "com.chiroerp.finance..",
                    "com.chiroerp.inventory..",
                    "com.chiroerp.tenancy.."
                )
                .should().onlyDependOnClassesThat().resideInAPackage("com.chiroerp.shared..")
                .allowEmptyShould(true)
                .because("ADR-006: Contexts should depend on interfaces from platform-shared, not implementations")

            rule.check(importedClasses)
        }
    }

    // =========================================================================
    // ALLOWED CONTENT VERIFICATION
    // =========================================================================

    @Nested
    @DisplayName("Allowed Content in platform-shared")
    inner class AllowedContent {

        @Test
        @DisplayName("common-types should contain only technical primitives")
        fun `common-types should contain technical primitives`() {
            // Verify that classes in common-types follow naming conventions for primitives
            val rule = classes()
                .that().resideInAPackage("com.chiroerp.shared.types..")
                .should(beTechnicalPrimitive())
                .allowEmptyShould(true)
                .because("ADR-006: common-types should contain Result, Command, Query, DomainEvent, etc.")

            rule.check(importedClasses)
        }
    }

    // =========================================================================
    // CUSTOM CONDITIONS
    // =========================================================================

    private fun notBeConcreteImplementation(): ArchCondition<JavaClass> {
        return object : ArchCondition<JavaClass>("be an interface, not a concrete implementation") {
            override fun check(item: JavaClass, events: ConditionEvents) {
                if (!item.isInterface && !item.modifiers.contains(com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT)) {
                    val message = "${item.fullName} is a concrete Service implementation in platform-shared"
                    events.add(SimpleConditionEvent.violated(item, message))
                }
            }
        }
    }

    private fun onlyDependOnPlatformSharedInterfaces(): ArchCondition<JavaClass> {
        return object : ArchCondition<JavaClass>("only depend on interfaces from platform-shared") {
            override fun check(item: JavaClass, events: ConditionEvents) {
                item.directDependenciesFromSelf
                    .filter { dep -> dep.targetClass.packageName.startsWith("com.chiroerp.shared") }
                    .filter { dep -> !dep.targetClass.isInterface }
                    .filter { dep -> !isAllowedSharedClass(dep.targetClass) }
                    .forEach { dep ->
                        val message = "${item.fullName} depends on non-interface ${dep.targetClass.fullName} from platform-shared"
                        events.add(SimpleConditionEvent.violated(item, message))
                    }
            }

            private fun isAllowedSharedClass(javaClass: JavaClass): Boolean {
                val allowedPatterns = listOf(
                    "Result", "DomainError", "ValidationError",  // Results
                    "DomainEvent", "EventMetadata", "EventEnvelope", "EventVersion",  // Events
                    "Command", "Query",  // CQRS
                    "AuditInfo", "CorrelationId", "Identifier",  // Primitives
                    "AggregateRoot", "Entity", "ValueObject"  // DDD base types
                )
                return allowedPatterns.any { pattern -> javaClass.simpleName.contains(pattern) }
            }
        }
    }

    private fun beTechnicalPrimitive(): ArchCondition<JavaClass> {
        return object : ArchCondition<JavaClass>("be a technical primitive or abstraction") {
            override fun check(item: JavaClass, events: ConditionEvents) {
                val technicalPatterns = listOf(
                    "Result", "Error", "Validation",
                    "Event", "Command", "Query", "Handler",
                    "Aggregate", "Entity", "ValueObject",
                    "Audit", "Correlation", "Identifier"
                )

                val forbiddenPatterns = listOf(
                    "Money", "Address", "Phone", "Email", "Tax",
                    "Customer", "Order", "Invoice", "Product"
                )

                val isTechnical = technicalPatterns.any { pattern -> item.simpleName.contains(pattern) }
                val isForbidden = forbiddenPatterns.any { pattern -> item.simpleName.contains(pattern) }

                if (isForbidden) {
                    val message = "${item.fullName} is a forbidden business type in common-types"
                    events.add(SimpleConditionEvent.violated(item, message))
                }
            }
        }
    }
}
