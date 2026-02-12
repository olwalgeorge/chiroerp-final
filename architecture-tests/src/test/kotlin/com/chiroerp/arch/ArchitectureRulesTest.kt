package com.chiroerp.arch

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaAnnotation
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaField
import com.tngtech.archunit.core.domain.JavaMember
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.members
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchitectureRulesTest {
    private val basePackages = arrayOf("com.chiroerp")

    private val importedClasses = ClassFileImporter()
        .withImportOption { location ->
            !location.contains("/build/")
        }
        .importPackages(*basePackages)

    @Test
    fun `domain layer should not depend on application or infrastructure`() {
        val rule = noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..", "io.quarkus..", "org.springframework..", "jakarta.ws.rs..", "jakarta.persistence..")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `domain layer should remain framework agnostic`() {
        val rule = noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("jakarta..", "io.quarkus..", "org.springframework..")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `application layer should not depend on infrastructure`() {
        val rule = noClasses()
            .that().resideInAnyPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "io.quarkus..", "org.springframework..", "jakarta.ws.rs..", "jakarta.persistence..")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `rest adapters should depend on use cases not repositories`() {
        val rule = noClasses()
            .that().resideInAnyPackage("..infrastructure.adapter.input.rest..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure.adapter.output..", "..infrastructure.persistence..", "..infrastructure.repository..")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `domain events should be named with Event suffix`() {
        val rule = classes()
            .that().resideInAnyPackage("..domain.events..")
            .should().haveSimpleNameEndingWith("Event")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `event publishers should use transactional outbox`() {
        val rule = classes()
            .that().resideInAnyPackage("..infrastructure.adapter.output.event..")
            .should(referenceOutboxType())
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `since annotated fields should be nullable`() {
        val rule = fields()
            .that().areAnnotatedWith(annotationNamed(".Since"))
            .should(beNullableField())
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `deprecated since should include replaceWith guidance`() {
        val rule = members()
            .that().areAnnotatedWith(annotationNamed(".DeprecatedSince"))
            .should(haveDeprecatedReplaceWith())
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `repositories should reference tenant context`() {
        val rule = classes()
            .that().haveSimpleNameContaining("Repository")
            .and().resideInAnyPackage("..infrastructure..")
            .should(referenceTenantType())
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    private fun annotationNamed(suffix: String): DescribedPredicate<JavaAnnotation<*>> {
        return DescribedPredicate.describe("annotation named $suffix") { annotation ->
            annotation.type.name.endsWith(suffix)
        }
    }

    private fun beNullableField(): ArchCondition<JavaField> {
        return object : ArchCondition<JavaField>("be nullable (no @NotNull/Nonnull and not primitive)") {
            override fun check(item: JavaField, events: ConditionEvents) {
                val isPrimitive = item.rawType.isPrimitive
                val hasNotNull = item.annotations.any { ann ->
                    val name = ann.rawType.name
                    name.endsWith(".NotNull") || name.endsWith(".Nonnull")
                }

                if (isPrimitive || hasNotNull) {
                    val message = "${item.owner.fullName}.${item.name} annotated with @Since must be nullable"
                    events.add(SimpleConditionEvent.violated(item, message))
                }
            }
        }
    }

    private fun haveDeprecatedReplaceWith(): ArchCondition<JavaMember> {
        return object : ArchCondition<JavaMember>("have non-empty replaceWith in @DeprecatedSince") {
            override fun check(item: JavaMember, events: ConditionEvents) {
                val annotation = item.annotations.firstOrNull { ann ->
                    ann.rawType.name.endsWith(".DeprecatedSince")
                } ?: return

                val replaceWith = annotation.get("replaceWith").orElse(null) as? String
                val trimmed = replaceWith?.trim().orEmpty()

                if (trimmed.length < 10) {
                    val message = "${item.owner.fullName}.${item.name} has @DeprecatedSince without meaningful replaceWith"
                    events.add(SimpleConditionEvent.violated(item, message))
                }
            }
        }
    }

    private fun referenceOutboxType(): ArchCondition<JavaClass> {
        return object : ArchCondition<JavaClass>("reference an Outbox type") {
            override fun check(item: JavaClass, events: ConditionEvents) {
                val hasOutboxDependency = item.directDependenciesFromSelf.any { dep ->
                    dep.targetClass.simpleName.contains("Outbox") || dep.targetClass.packageName.contains("outbox")
                }

                if (!hasOutboxDependency) {
                    val message = "${item.fullName} should publish via transactional outbox"
                    events.add(SimpleConditionEvent.violated(item, message))
                }
            }
        }
    }

    private fun referenceTenantType(): ArchCondition<JavaClass> {
        return object : ArchCondition<JavaClass>("reference a Tenant type in repository") {
            override fun check(item: JavaClass, events: ConditionEvents) {
                val hasTenantDependency = item.directDependenciesFromSelf.any { dep ->
                    dep.targetClass.simpleName.contains("Tenant")
                }

                if (!hasTenantDependency) {
                    val message = "${item.fullName} does not reference a Tenant type (TenantId/TenantContext)"
                    events.add(SimpleConditionEvent.violated(item, message))
                }
            }
        }
    }

    // =============================================================================
    // INFRASTRUCTURE LAYER RULES
    // =============================================================================

    @Test
    fun `REST controllers should be in infrastructure api package`() {
        val rule = classes()
            .that().haveSimpleNameEndingWith("Resource")
            .or().haveSimpleNameEndingWith("Controller")
            .should().resideInAnyPackage("..infrastructure.api..", "..infrastructure.adapter.input.rest..")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `Kafka consumers should be in infrastructure messaging package`() {
        val rule = classes()
            .that().haveSimpleNameEndingWith("Consumer")
            .or().haveSimpleNameEndingWith("Listener")
            .should().resideInAnyPackage("..infrastructure.messaging..", "..infrastructure.adapter.input.kafka..")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `Kafka producers should be in infrastructure messaging package`() {
        val rule = classes()
            .that().haveSimpleNameEndingWith("Producer")
            .or().haveSimpleNameEndingWith("Publisher")
            .should().resideInAnyPackage("..infrastructure.messaging..", "..infrastructure.adapter.output.event..")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `outbox writers should only use domain events`() {
        val rule = classes()
            .that().haveSimpleNameContaining("Outbox")
            .and().resideInAnyPackage("..infrastructure..")
            .should(onlyDependOnDomainEvents())
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `infrastructure should not call other bounded contexts directly`() {
        val rule = classes()
            .that().resideInAnyPackage("..infrastructure..")
            .should(notCallOtherBoundedContextsDirectly())
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `repositories should not expose persistence entities`() {
        val rule = classes()
            .that().haveSimpleNameEndingWith("Repository")
            .and().resideInAnyPackage("..infrastructure..")
            .should(notReturnPersistenceEntities())
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    private fun onlyDependOnDomainEvents(): ArchCondition<JavaClass> {
        return object : ArchCondition<JavaClass>("only depend on domain events, not direct database writes") {
            override fun check(item: JavaClass, events: ConditionEvents) {
                // Check if outbox writer directly writes to database (should use events instead)
                val hasDirectDbWrite = item.directDependenciesFromSelf.any { dep ->
                    dep.targetClass.name.contains("EntityManager") || 
                    dep.targetClass.name.contains("JdbcTemplate") ||
                    dep.targetClass.name.contains("Repository")
                }

                if (hasDirectDbWrite) {
                    val message = "${item.fullName} should not write directly to DB, use domain events"
                    events.add(SimpleConditionEvent.violated(item, message))
                }
            }
        }
    }

    private fun notCallOtherBoundedContextsDirectly(): ArchCondition<JavaClass> {
        return object : ArchCondition<JavaClass>("not call other bounded contexts directly") {
            override fun check(item: JavaClass, events: ConditionEvents) {
                val sourceContext = extractBoundedContext(item.packageName)

                item.directDependenciesFromSelf.forEach { dep ->
                    val targetPackage = dep.targetClass.packageName
                    val targetContext = extractBoundedContext(targetPackage)

                    // Check if calling infrastructure of different context
                    if (sourceContext != null && targetContext != null && sourceContext != targetContext) {
                        if (targetPackage.contains(".infrastructure.")) {
                            val message = "${item.fullName} calls ${dep.targetClass.fullName} from different bounded context"
                            events.add(SimpleConditionEvent.violated(item, message))
                        }
                    }
                }
            }
        }
    }

    private fun notReturnPersistenceEntities(): ArchCondition<JavaClass> {
        return object : ArchCondition<JavaClass>("not expose JPA/persistence entities in public API") {
            override fun check(item: JavaClass, events: ConditionEvents) {
                item.methods.forEach { method ->
                    if (method.modifiers.contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC)) {
                        val returnType = method.rawReturnType
                        val hasEntityAnnotation = returnType.annotations.any { ann ->
                            ann.rawType.name == "jakarta.persistence.Entity"
                        }

                        if (hasEntityAnnotation) {
                            val message = "${item.fullName}.${method.name}() returns JPA entity ${returnType.name}"
                            events.add(SimpleConditionEvent.violated(item, message))
                        }
                    }
                }
            }
        }
    }

    private fun extractBoundedContext(packageName: String): String? {
        // Extract bounded context from package like com.chiroerp.finance.gl.infrastructure
        val parts = packageName.split(".")
        val chiroIndex = parts.indexOf("chiroerp")
        if (chiroIndex != -1 && parts.size > chiroIndex + 2) {
            return "${parts[chiroIndex + 1]}.${parts[chiroIndex + 2]}" // e.g., "finance.gl"
        }
        return null
    }
}
