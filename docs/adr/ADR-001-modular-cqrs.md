# ADR-001: Pragmatic CQRS Implementation

**Status**: Draft (Not Implemented; Revised 2025-11-16)  
**Date**: 2025-11-05 (Original), 2025-11-16 (Revised)  
**Deciders**: Architecture Team  
**Tier**: Core  
**Tags**: cqrs, architecture, use-cases, validation  

## Context
The ERP platform requires handling complex business workflows spanning multiple bounded contexts. We need to implement the Command Query Responsibility Segregation (CQRS) pattern while maintaining consistency, testability, developer ergonomics, and team velocity.

**2025-11-16 Revision**: After design review and prototyping, we identified that the original design with command bus abstraction added unnecessary complexity. This revision documents the pragmatic approach selected for implementation, which better serves our needs.

## Decision
We will implement a **pragmatic CQRS pattern** using direct use case invocation with Jakarta Bean Validation:

1. **Commands and Queries as Data Classes**
   - Immutable Kotlin data classes with validation annotations
   - Jakarta Bean Validation (@NotNull, @Valid, @Size, @Pattern, etc.)
   - Type-safe domain value objects (TenantId, UserId, etc.)
   - No command bus abstraction (YAGNI principle)

2. **Use Case Pattern**
   - Direct use case classes handle commands/queries
   - Use cases injected via CDI (Quarkus Arc)
   - Clear method signatures (execute, handle, process)
   - One use case per business operation

3. **Cross-Cutting Concerns**
   - Validation at REST boundary (@Valid @BeanParam)
   - Transaction management via @Transactional annotation
   - Logging and tracing via Quarkus interceptors
   - Audit trail via domain events

4. **Implementation Strategy**
   - Commands/Queries in `*-application/port/input/`
   - Use cases in `*-application/service/` or `*-application/usecase/`
   - Domain logic stays in `*-domain/` layer
   - REST adapters in `*-infrastructure/adapter/input/rest/`

### Why This is Better Than Command Bus

**Our Pragmatic Approach:**
```kotlin
@Path("/users")
class UserResource {
    @Inject
    lateinit var createUserUseCase: CreateUserUseCase
    
    @POST
    fun createUser(@Valid @BeanParam request: CreateUserRequest): Response {
        val command = request.toCommand(locale)
        val userId = createUserUseCase.execute(command)
        return Response.created(userId.toUri()).build()
    }
}
```

**Command Bus Approach (More Complex):**
```kotlin
@Path("/users")
class UserResource {
    @Inject
    lateinit var commandBus: CommandBus
    
    @POST
    fun createUser(@Valid @BeanParam request: CreateUserRequest): Response {
        val command = request.toCommand(locale)
        val userId = commandBus.dispatch<CreateUserCommand, UserId>(command)
        return Response.created(userId.toUri()).build()
    }
}
```

**Benefits of Direct Use Case:**
- ✅ No generic dispatch logic to debug
- ✅ Clear in stack traces (direct method calls)
- ✅ Faster execution (no reflection/routing)
- ✅ Better type safety (no generic type erasure issues)
- ✅ Simpler for junior developers to understand

### Comparison with Industry Standards

### SAP S/4HANA Pattern
SAP uses direct Business Object Processing Framework (BOPF) calls:
```abap
" SAP doesn't use command bus
CALL METHOD /bobf/cl_lib_d_write_srv=>modify
  EXPORTING
    it_modification = lt_modification
  IMPORTING
    es_message      = ls_message.
```

Our approach mirrors this simplicity while adding modern validation.

### Spring Boot Typical Pattern
```java
@RestController
public class UserController {
    private final UserService userService;  // Direct injection
    
    @PostMapping("/users")
    public ResponseEntity<UserId> create(@Valid @RequestBody CreateUserRequest request) {
        UserId userId = userService.createUser(request);  // Direct call
        return ResponseEntity.created(userId).build();
    }
}
```

Our pattern is consistent with mainstream enterprise practices.

### Testing Strategy

### Unit Testing Commands/Queries
```kotlin
class CreateUserCommandTest {
    @Test
    fun `should validate required fields`() {
        val validator = Validation.buildDefaultValidatorFactory().validator
        
        val invalidCommand = CreateUserCommand(
            tenantId = null,  // Invalid
            username = "",     // Invalid
            email = "not-an-email",  // Invalid
            fullName = "A",    // Too short
            password = "123"   // Too short
        )
        
        val violations = validator.validate(invalidCommand)
        assertTrue(violations.isNotEmpty())
    }
}
```

### Unit Testing Use Cases
```kotlin
class CreateUserUseCaseTest {
    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val eventPublisher = mockk<DomainEventPublisher>()
    private val useCase = CreateUserUseCase(userRepository, passwordEncoder, eventPublisher)
    
    @Test
    fun `should create user and publish event`() {
        // Given
        val command = validCreateUserCommand()
        every { passwordEncoder.encode(any()) } returns "hashed"
        every { userRepository.save(any()) } returns Unit
        every { eventPublisher.publish(any()) } returns Unit
        
        // When
        val userId = useCase.execute(command)
        
        // Then
        assertNotNull(userId)
        verify { userRepository.save(any()) }
        verify { eventPublisher.publish(ofType<UserCreatedEvent>()) }
    }
}
```

### Integration Testing REST→UseCase→Domain
```kotlin
@QuarkusTest
class UserResourceIT {
    @Test
    fun `POST /users should create user`() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "username": "john.doe",
                  "email": "john@example.com",
                  "fullName": "John Doe",
                  "password": "SecurePass123!"
                }
            """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .header("Location", containsString("/users/"))
    }
}
```

### Architecture Decision Records (Meta)

### Why Revise This ADR?

**Date**: 2025-11-16  
**Trigger**: Design review revealed documentation mismatch with intended implementation

**Findings**:
1. Planned commands should be simple data classes with validation annotations
2. Command bus abstraction is unnecessary for initial phases
3. Direct use case invocation is simpler and more maintainable
4. Aligns with industry practices (SAP, Spring Boot)

**Decision**: Update ADR to document intended implementation
**Rationale**: Documentation should reflect the agreed design, not aspirational alternatives

### Lessons Learned

1. **YAGNI Principle Validated**: We didn't need command bus abstraction
2. **Simplicity Wins**: Simpler code is easier to debug, test, and maintain
3. **Pragmatism > Purity**: Enterprise software favors practical patterns
4. **Team Velocity**: Reduced cognitive load improves productivity
5. **Industry Alignment**: Our pattern matches proven enterprise practices

## Alternatives Considered
### 1. Command Bus with Handler Registry
**Rejected (Original Choice, Now Revised)**: While theoretically elegant, it adds:
- Unnecessary indirection (harder debugging)
- Performance overhead (handler lookup, reflection)
- Complexity without proportional benefit
- Learning curve for team members
- Generic type erasure issues in Kotlin/Java

**Analysis**: SAP S/4HANA and other enterprise systems use direct service calls, not command buses. The command bus pattern is valuable in distributed systems with asynchronous processing, but our use case is synchronous REST→UseCase→Domain.

### 2. No CQRS at All (Traditional Services)
**Rejected**: Lack of explicit command/query separation makes it harder to:
- Apply different optimization strategies for reads vs writes
- Maintain audit trails
- Enforce validation consistently
- Scale reads and writes independently

### 3. Full Event Sourcing from Day One
**Rejected**: Too complex for initial implementation. Our pragmatic CQRS provides the foundation to add event sourcing later if specific aggregates need it.

### 4. Third-Party CQRS Framework (Axon, etc.)
**Rejected**: 
- Vendor lock-in
- Heavy dependencies
- Opinionated structure
- Less control over implementation
- Our needs are specific enough to warrant custom approach

## Consequences
### Positive
- ✅ Clear separation between reads and writes (CQRS core benefit)
- ✅ Simplified debugging (no command bus indirection)
- ✅ Better IDE support (direct method calls, auto-completion)
- ✅ Faster development velocity (less boilerplate)
- ✅ Type-safe validation at compile time
- ✅ Explicit business operations via command data classes
- ✅ Excellent testability (mock use cases directly)
- ✅ Lower cognitive overhead for new developers
- ✅ Better performance (no bus routing overhead)

### Negative
- ❌ No centralized command dispatch point (acceptable trade-off)
- ❌ Manual use case wiring (mitigated by CDI auto-injection)
- ❌ Cannot easily add generic command interceptors (use REST filters instead)

### Neutral
- ⚖️ Can add command bus later if truly needed (start simple)
- ⚖️ Can migrate to event sourcing if required
- ⚖️ Validation happens at REST boundary (not in command layer)

## Compliance
Enforce via architecture review and (planned) ArchUnit rules:
- Commands/queries are data classes without a command bus abstraction.
- Use cases live in the application layer and are injected via CDI.
- REST adapters do not bypass use cases to access domain internals.
Exceptions require an ADR update and explicit approval.

## Implementation Plan
### Implementation Notes

### Command Example (Actual Implementation)
```kotlin
// In tenancy-identity/identity-application/port/input/command/
package com.erp.identity.application.port.input.command

import com.erp.identity.domain.model.identity.RoleId
import com.erp.identity.domain.model.tenant.TenantId
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateUserCommand(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: TenantId,
    
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be 3-50 characters long")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Username can contain letters, numbers, underscore, and hyphen only"
    )
    val username: String,
    
    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:NotBlank(message = "Full name is required")
    @field:Size(min = 2, max = 200)
    val fullName: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 256)
    val password: String,
    
    val roleIds: Set<RoleId> = emptySet()
)
```

### Use Case Example (Actual Implementation)
```kotlin
// In tenancy-identity/identity-application/service/
package com.erp.identity.application.service

import com.erp.identity.domain.model.identity.User
import com.erp.identity.domain.model.identity.UserId
import com.erp.identity.domain.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class CreateUserUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: DomainEventPublisher
) {
    @Transactional
    fun execute(command: CreateUserCommand): UserId {
        // Validation already done at REST boundary
        
        // Domain logic
        val hashedPassword = passwordEncoder.encode(command.password)
        val user = User.create(
            tenantId = command.tenantId,
            username = command.username,
            email = command.email,
            fullName = command.fullName,
            hashedPassword = hashedPassword
        )
        
        // Assign roles
        command.roleIds.forEach { roleId ->
            user.assignRole(roleId)
        }
        
        // Persist
        userRepository.save(user)
        
        // Publish events
        user.domainEvents.forEach { event ->
            eventPublisher.publish(event)
        }
        user.clearDomainEvents()
        
        return user.id
    }
}
```

### REST Integration (Actual Implementation)
```kotlin
// In tenancy-identity/identity-infrastructure/adapter/input/rest/
package com.erp.identity.infrastructure.adapter.input.rest

import com.erp.identity.application.port.input.command.CreateUserCommand
import com.erp.identity.application.service.CreateUserUseCase
import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import java.util.Locale

@Path("/users")
@Produces("application/json")
@Consumes("application/json")
class UserResource {
    @Inject
    lateinit var createUserUseCase: CreateUserUseCase
    
    @Context
    lateinit var httpHeaders: HttpHeaders
    
    @POST
    fun createUser(@Valid request: CreateUserRequest): Response {
        // Transform REST request to command
        val command = request.toCommand(currentLocale())
        
        // Execute use case
        val userId = createUserUseCase.execute(command)
        
        // Return response
        return Response
            .created(URI.create("/users/${userId.value}"))
            .entity(mapOf("userId" to userId.value))
            .build()
    }
    
    private fun currentLocale(): Locale {
        return httpHeaders.acceptableLanguages.firstOrNull()?.let {
            Locale.forLanguageTag(it.language)
        } ?: Locale.ENGLISH
    }
}

// Request DTO with transformation method
data class CreateUserRequest(
    @field:NotBlank val username: String,
    @field:Email val email: String,
    @field:NotBlank val fullName: String,
    @field:NotBlank val password: String,
    val roleIds: Set<String> = emptySet()
) {
    fun toCommand(tenantId: TenantId, locale: Locale): CreateUserCommand {
        return CreateUserCommand(
            tenantId = tenantId,
            username = username.trim(),
            email = email.trim().lowercase(),
            fullName = fullName.trim(),
            password = password,
            roleIds = roleIds.map { RoleId(UUID.fromString(it)) }.toSet()
        )
    }
}
```

### Query Example (Actual Implementation)
```kotlin
// Query data class
data class GetUserQuery(
    @field:NotNull
    val tenantId: TenantId,
    
    @field:NotNull
    val userId: UserId
)

// Query use case
@ApplicationScoped
class GetUserUseCase(
    private val userRepository: UserRepository
) {
    fun execute(query: GetUserQuery): UserDTO {
        val user = userRepository.findById(query.tenantId, query.userId)
            ?: throw UserNotFoundException(query.userId)
        
        return UserDTO.from(user)
    }
}
```

## References

### Related ADRs
- docs/adr/ADR-010-rest-validation-standard.md - REST validation patterns

### Internal Documentation
- SAP Business Object Processing Framework (BOPF) - SAP's approach
- bounded-contexts/tenancy-identity/identity-application/ - Planned reference layout
- platform-shared/common-types/src/main/kotlin/cqrs/ - Marker interfaces (optional use)

### External References
- 2025-11-05: Initial version with command bus design
- 2025-11-16: Revised to document pragmatic use case pattern
- [Greg Young: CQRS Documents](https://cqrs.files.wordpress.com/2010/11/cqrs_documents.pdf) - CQRS deep dive
- [Jakarta Bean Validation Specification](https://jakarta.ee/specifications/bean-validation/3.0/) - Validation standard
- [Martin Fowler: CQRS](https://martinfowler.com/bliki/CQRS.html) - Core CQRS concepts
- [Quarkus CDI Reference](https://quarkus.io/guides/cdi-reference) - Dependency injection
- Revision History:
