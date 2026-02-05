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
}
