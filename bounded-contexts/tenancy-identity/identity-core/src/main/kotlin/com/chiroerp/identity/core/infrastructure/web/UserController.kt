package com.chiroerp.identity.core.infrastructure.web

import com.chiroerp.identity.core.application.exception.TenantScopeViolationException
import com.chiroerp.identity.core.application.exception.UserAlreadyExistsException
import com.chiroerp.identity.core.application.exception.UserNotFoundException
import com.chiroerp.identity.core.application.handler.UserCommandHandler
import com.chiroerp.identity.core.application.handler.UserQueryHandler
import com.chiroerp.identity.core.application.query.GetActiveSessionsQuery
import com.chiroerp.identity.core.application.query.GetUserByEmailQuery
import com.chiroerp.identity.core.application.query.GetUserPermissionsQuery
import com.chiroerp.identity.core.application.query.GetUserQuery
import com.chiroerp.identity.core.application.query.ListUsersQuery
import com.chiroerp.identity.core.application.service.PasswordService
import com.chiroerp.identity.core.domain.model.UserStatus
import jakarta.validation.Valid
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.UUID

@Path("/api/identity/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UserController(
    private val userCommandHandler: UserCommandHandler,
    private val userQueryHandler: UserQueryHandler,
    private val passwordService: PasswordService,
) {
    @POST
    fun createUser(@Valid request: CreateUserRequest): Response {
        return try {
            val created = userCommandHandler.handle(
                request.toCommand(passwordHash = passwordService.hash(request.password)),
            )

            val view = userQueryHandler.handle(
                GetUserQuery(
                    tenantId = request.tenantId,
                    userId = created.id.value,
                ),
            )

            Response.status(Response.Status.CREATED)
                .entity(UserResponse.from(view))
                .build()
        } catch (ex: UserAlreadyExistsException) {
            throw WebApplicationException(ex.message, Response.Status.CONFLICT)
        } catch (ex: IllegalArgumentException) {
            throw BadRequestException(ex.message)
        }
    }

    @GET
    @Path("/{userId}")
    fun getUser(
        @PathParam("userId") rawUserId: String,
        @QueryParam("tenantId") rawTenantId: String,
    ): UserResponse {
        val userId = parseUuid(rawUserId, "userId")
        val tenantId = parseUuid(rawTenantId, "tenantId")

        return try {
            UserResponse.from(
                userQueryHandler.handle(
                    GetUserQuery(
                        tenantId = tenantId,
                        userId = userId,
                    ),
                ),
            )
        } catch (ex: UserNotFoundException) {
            throw NotFoundException(ex.message)
        } catch (ex: TenantScopeViolationException) {
            throw WebApplicationException(ex.message, Response.Status.FORBIDDEN)
        }
    }

    @GET
    fun listUsers(
        @QueryParam("tenantId") rawTenantId: String,
        @QueryParam("offset") offset: Int?,
        @QueryParam("limit") limit: Int?,
        @QueryParam("status") rawStatus: String?,
        @QueryParam("email") email: String?,
    ): UserListResponse {
        val tenantId = parseUuid(rawTenantId, "tenantId")

        email?.trim()?.takeIf { it.isNotEmpty() }?.let { normalizedEmail ->
            val user = userQueryHandler.handle(
                GetUserByEmailQuery(
                    tenantId = tenantId,
                    email = normalizedEmail,
                ),
            )

            val items = user?.let { listOf(UserResponse.from(it)) } ?: emptyList()
            return UserListResponse(
                items = items,
                offset = 0,
                limit = 1,
                count = items.size,
            )
        }

        val parsedStatus = rawStatus?.trim()?.takeIf { it.isNotEmpty() }?.let {
            runCatching { UserStatus.valueOf(it.uppercase()) }
                .getOrElse { throw BadRequestException("Invalid status '$it'") }
        }

        val normalizedOffset = (offset ?: 0).coerceAtLeast(0)
        val normalizedLimit = (limit ?: 50).coerceIn(1, 500)

        val users = userQueryHandler.handle(
            ListUsersQuery(
                tenantId = tenantId,
                offset = normalizedOffset,
                limit = normalizedLimit,
                status = parsedStatus,
            ),
        )

        return UserListResponse(
            items = users.map(UserResponse::from),
            offset = normalizedOffset,
            limit = normalizedLimit,
            count = users.size,
        )
    }

    @GET
    @Path("/{userId}/permissions")
    fun getPermissions(
        @PathParam("userId") rawUserId: String,
        @QueryParam("tenantId") rawTenantId: String,
    ): UserPermissionsResponse {
        val userId = parseUuid(rawUserId, "userId")
        val tenantId = parseUuid(rawTenantId, "tenantId")

        return try {
            UserPermissionsResponse.from(
                userQueryHandler.handle(
                    GetUserPermissionsQuery(
                        tenantId = tenantId,
                        userId = userId,
                    ),
                ),
            )
        } catch (ex: UserNotFoundException) {
            throw NotFoundException(ex.message)
        } catch (ex: TenantScopeViolationException) {
            throw WebApplicationException(ex.message, Response.Status.FORBIDDEN)
        }
    }

    @GET
    @Path("/{userId}/sessions")
    fun getActiveSessions(
        @PathParam("userId") rawUserId: String,
        @QueryParam("tenantId") rawTenantId: String,
    ): List<ActiveSessionResponse> {
        val userId = parseUuid(rawUserId, "userId")
        val tenantId = parseUuid(rawTenantId, "tenantId")

        return try {
            userQueryHandler.handle(
                GetActiveSessionsQuery(
                    tenantId = tenantId,
                    userId = userId,
                ),
            ).map(ActiveSessionResponse::from)
        } catch (ex: UserNotFoundException) {
            throw NotFoundException(ex.message)
        } catch (ex: TenantScopeViolationException) {
            throw WebApplicationException(ex.message, Response.Status.FORBIDDEN)
        }
    }

    @POST
    @Path("/{userId}/activate")
    fun activateUser(
        @PathParam("userId") rawUserId: String,
        @Valid request: ActivateUserRequest,
    ): UserResponse = mutateAndFetch(rawUserId, request.tenantId) { userId ->
        userCommandHandler.handle(request.toCommand(userId))
    }

    @POST
    @Path("/{userId}/lock")
    fun lockUser(
        @PathParam("userId") rawUserId: String,
        @Valid request: LockUserRequest,
    ): UserResponse = mutateAndFetch(rawUserId, request.tenantId) { userId ->
        userCommandHandler.handle(request.toCommand(userId))
    }

    @POST
    @Path("/{userId}/roles")
    fun assignRole(
        @PathParam("userId") rawUserId: String,
        @Valid request: AssignRoleRequest,
    ): UserResponse = mutateAndFetch(rawUserId, request.tenantId) { userId ->
        userCommandHandler.handle(request.toCommand(userId))
    }

    @POST
    @Path("/{userId}/password")
    fun changePassword(
        @PathParam("userId") rawUserId: String,
        @Valid request: ChangePasswordRequest,
    ): UserResponse = mutateAndFetch(rawUserId, request.tenantId) { userId ->
        val passwordHash = passwordService.hash(request.newPassword)
        userCommandHandler.handle(request.toCommand(userId, passwordHash))
    }

    @POST
    @Path("/{userId}/external-identities")
    fun linkExternalIdentity(
        @PathParam("userId") rawUserId: String,
        @Valid request: LinkExternalIdentityRequest,
    ): UserResponse = mutateAndFetch(rawUserId, request.tenantId) { userId ->
        userCommandHandler.handle(request.toCommand(userId))
    }

    private fun mutateAndFetch(
        rawUserId: String,
        tenantId: UUID,
        operation: (UUID) -> Any,
    ): UserResponse {
        val userId = parseUuid(rawUserId, "userId")

        return try {
            operation(userId)

            val view = userQueryHandler.handle(
                GetUserQuery(
                    tenantId = tenantId,
                    userId = userId,
                ),
            )
            UserResponse.from(view)
        } catch (ex: UserNotFoundException) {
            throw NotFoundException(ex.message)
        } catch (ex: TenantScopeViolationException) {
            throw WebApplicationException(ex.message, Response.Status.FORBIDDEN)
        } catch (ex: IllegalArgumentException) {
            throw BadRequestException(ex.message)
        }
    }

    private fun parseUuid(rawValue: String?, field: String): UUID {
        val trimmed = rawValue?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            throw BadRequestException("Missing required parameter '$field'")
        }

        return runCatching { UUID.fromString(trimmed) }
            .getOrElse { throw BadRequestException("Invalid UUID for '$field': $rawValue") }
    }
}
