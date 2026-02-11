package com.chiroerp.identity.core.application.handler

import com.chiroerp.identity.core.application.command.ActivateUserCommand
import com.chiroerp.identity.core.application.command.AssignRoleCommand
import com.chiroerp.identity.core.application.command.ChangePasswordCommand
import com.chiroerp.identity.core.application.command.CreateUserCommand
import com.chiroerp.identity.core.application.command.PermissionGrant
import com.chiroerp.identity.core.application.command.RoleAssignment
import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.exception.UserAlreadyExistsException
import com.chiroerp.identity.core.application.exception.UserLifecycleException
import com.chiroerp.identity.core.application.exception.UserNotFoundException
import com.chiroerp.identity.core.domain.event.UserDomainEvent
import com.chiroerp.identity.core.domain.model.Permission
import com.chiroerp.identity.core.domain.model.User
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserProfile
import com.chiroerp.identity.core.domain.model.UserRole
import com.chiroerp.identity.core.domain.port.UserEventPublisher
import com.chiroerp.identity.core.domain.port.UserRepository
import com.chiroerp.tenancy.shared.TenantId
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

@ApplicationScoped
class UserCommandHandler(
    private val userRepository: UserRepository,
    private val userEventPublisher: UserEventPublisher,
) {
    @Transactional
    fun handle(command: CreateUserCommand): User {
        val tenantId = TenantId(command.tenantId)
        val normalizedEmail = command.email.trim().lowercase(Locale.ROOT)

        userRepository.findByEmail(tenantId, normalizedEmail)?.let {
            throw UserAlreadyExistsException(tenantId, normalizedEmail)
        }

        val profile = UserProfile(
            firstName = command.firstName.trim(),
            lastName = command.lastName.trim(),
            email = normalizedEmail,
            phoneNumber = command.phoneNumber,
            locale = command.locale,
            timeZone = ZoneId.of(command.timeZoneId),
        )

        val initialRoles = command.roles.map { it.toDomainRole() }.toSet()
        val directPermissions = command.directPermissions.map { it.toDomainPermission() }.toSet()

        val user = User.register(
            tenantId = tenantId,
            profile = profile,
            passwordHash = command.passwordHash,
            initialRoles = initialRoles,
            initialPermissions = directPermissions,
        )

        val saved = userRepository.save(user)
        publish(user.pullDomainEvents())
        return saved
    }

    @Transactional
    fun handle(command: ActivateUserCommand): User {
        return mutateUser(command.userId, command.tenantId) {
            try {
                it.activate()
            } catch (ex: IllegalStateException) {
                throw UserLifecycleException(it.id, ex.message ?: "Activation rejected")
            }
        }
    }

    @Transactional
    fun handle(command: AssignRoleCommand): User {
        return mutateUser(command.userId, command.tenantId) {
            it.assignRole(command.role.toDomainRole())
        }
    }

    @Transactional
    fun handle(command: ChangePasswordCommand): User {
        return mutateUser(command.userId, command.tenantId) {
            val historySize = command.historySize ?: DEFAULT_PASSWORD_HISTORY
            it.changePassword(
                newHash = command.newPasswordHash,
                changedAt = Instant.now(),
                historySize = historySize,
                rotationTtl = command.newTtl,
            )
            if (command.forceChangeOnNextLogin) {
                it.forcePasswordReset()
            }
        }
    }

    private fun mutateUser(userIdRaw: UUID, tenantIdRaw: UUID, operation: (User) -> Unit): User {
        val userId = UserId(userIdRaw)
        val tenantId = TenantId(tenantIdRaw)

        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException(userId)

        if (user.tenantId != tenantId) {
            throw TenantScopeViolationException(tenantId)
        }

        operation(user)

        val saved = userRepository.save(user)
        publish(user.pullDomainEvents())
        return saved
    }

    private fun RoleAssignment.toDomainRole(): UserRole = UserRole(
        code = code.trim().uppercase(Locale.ROOT),
        description = description?.trim()?.takeUnless { it.isEmpty() },
        permissions = permissions.map { it.toDomainPermission() }.toSet(),
        sodGroup = sodGroup,
    )

    private fun PermissionGrant.toDomainPermission(): Permission = Permission(
        objectId = objectId.trim().uppercase(Locale.ROOT),
        actions = actions.map { it.trim().uppercase(Locale.ROOT) }.toSet(),
        constraints = constraints.filterKeys { it.isNotBlank() },
    )

    private fun publish(events: List<UserDomainEvent>) {
        if (events.isNotEmpty()) {
            userEventPublisher.publish(events)
        }
    }

    companion object {
        private const val DEFAULT_PASSWORD_HISTORY = 5
    }
}
