# PropNest — IAM Module: Complete Project Guide

> A complete, beginner-friendly walkthrough of the Identity & Access Management (IAM) module: what it is, how it is structured, every layer, every important class, the meaning of every annotation, the end-to-end request flow, the database, and 100 interview questions with answers.

---

# PART 1 — PROJECT OVERVIEW

## 1.1 What is this project?

**PropNest** is a property-management backend application. The part you built is the **IAM module** — *Identity and Access Management*. IAM is the security backbone of any application: it answers two questions for every request:

- **Authentication ("Who are you?")** — proving a user is who they claim to be (login with email + password).
- **Authorization ("What are you allowed to do?")** — checking whether that user has permission to perform an action (e.g. only an admin can manage users).

So the IAM module handles: **user registration, login, logout, token refresh, profile management, password change, admin user management, role lookup, and audit logging**.

## 1.2 What does it actually do? (Features)

| Feature | Who can use it | Description |
|---|---|---|
| Register | Public | Create a new user account. |
| Login | Public | Verify credentials, return an access token + refresh token. |
| Refresh token | Semi-public | Exchange a valid refresh token for a new access token. |
| Logout | Logged-in user | Revoke the refresh token (session). |
| View / update own profile | Logged-in user | Read or edit own name and phone. |
| Change own password | Logged-in user | Update password after verifying the current one. |
| Create / list / view users | Admin only | Manage all accounts. |
| Update user status / role | Admin only | Activate/suspend a user or change their role. |
| List roles | Admin only | View the 6 fixed roles. |
| View audit logs | Admin only | Review security-relevant actions. |

## 1.3 Technology stack (and why each is used)

| Technology | Version | Why it is used in this project |
|---|---|---|
| **Java** | 21 | The programming language. Modern features used: records, enums, streams, `Optional`, `var`-free clean code. |
| **Spring Framework** | (via Boot) | Core engine: Inversion of Control container, Dependency Injection, transactions. |
| **Spring Boot** | 4.0.6 | Auto-configuration, embedded server, starters — lets the app run with minimal setup. |
| **Spring Data JPA** | (starter) | Database access without writing SQL — repositories + Hibernate ORM. |
| **Hibernate** | (via JPA) | The ORM implementation that maps Java objects to database tables. |
| **MySQL** | 8.x | The relational database that stores users, roles, sessions and audit logs. |
| **Maven** | 3.x (wrapper) | Build tool: manages dependencies and packages the app into a runnable JAR. |
| **JJWT** | 0.12.6 | Library to create and validate JWT access tokens. |
| **Spring Security Crypto** | (starter parent) | Provides `BCryptPasswordEncoder` for password hashing. |
| **Lombok** | (latest) | Removes boilerplate (getters, setters, constructors, builders). |
| **H2** | (test) | In-memory database used only for tests. |

> **Important talking point:** This project deliberately uses **only** `spring-security-crypto` (for BCrypt) and does **not** enable full Spring Security auto-configuration. Authentication is handled by the module's own custom **JWT filter**. This is documented in `SecurityBeansConfig`.

---

# PART 2 — PROJECT STRUCTURE & ARCHITECTURE

## 2.1 Folder structure

```
pod2-propNest/
├── pom.xml                          ← Maven build file (dependencies, plugins)
├── mvnw / mvnw.cmd                  ← Maven wrapper (run Maven without installing it)
└── src/
    ├── main/
    │   ├── java/com/cog/propNest/
    │   │   ├── PropNestApplication.java        ← Application entry point (main method)
    │   │   ├── common/                         ← Shared, module-independent code
    │   │   │   ├── exception/                  ← Generic exceptions + GlobalExceptionHandler
    │   │   │   └── response/                   ← ApiResponse, ErrorResponse wrappers
    │   │   ├── config/
    │   │   │   └── SecurityBeansConfig.java     ← Defines the PasswordEncoder bean
    │   │   └── module/identityAccessManagement/ ← THE IAM MODULE
    │   │       ├── config/      RoleSeeder.java  ← Seeds the 6 roles at startup
    │   │       ├── controller/                  ← REST endpoints (web layer)
    │   │       ├── dto/                          ← Request/response data objects
    │   │       ├── entity/                       ← Database tables as Java classes
    │   │       ├── exception/                    ← IAM-specific exceptions + handler
    │   │       ├── repository/                   ← Data-access interfaces
    │   │       ├── security/                     ← JWT, filter, auth context, guards
    │   │       └── service/                      ← Business logic
    │   └── resources/
    │       └── application.properties           ← Configuration (DB, JWT, server)
    └── test/                                     ← Unit and integration tests
```

## 2.2 The layered architecture (the most important diagram)

This project follows the classic **layered (N-tier) architecture**. Data flows top to bottom; each layer talks only to the layer directly below it.

```
   HTTP request (JSON)
          │
          ▼
┌─────────────────────┐
│  SECURITY FILTER     │  JwtAuthenticationFilter — validates token, sets AuthContext
└─────────────────────┘
          │
          ▼
┌─────────────────────┐
│  CONTROLLER layer    │  @RestController — receives request, validates input,
│  (web / presentation)│  returns ResponseEntity. NO business logic here.
└─────────────────────┘
          │  (passes DTOs)
          ▼
┌─────────────────────┐
│  SERVICE layer       │  @Service — the business logic & rules, @Transactional.
│  (business logic)    │  Talks to repositories, encodes passwords, mints tokens.
└─────────────────────┘
          │  (uses repositories)
          ▼
┌─────────────────────┐
│  REPOSITORY layer    │  extends JpaRepository — data access, no SQL written.
│  (persistence / DAO) │  Spring Data generates the queries.
└─────────────────────┘
          │  (Hibernate ORM)
          ▼
┌─────────────────────┐
│  DATABASE (MySQL)    │  Tables: users, roles, user_session, audit_log
└─────────────────────┘
```

**Supporting players that cut across layers:**
- **Entity** — Java classes mapped to DB tables (`User`, `Role`, ...).
- **DTO** — Data Transfer Objects: the JSON shapes that enter/leave the API (so entities are never exposed directly).
- **Exception handlers** — translate thrown exceptions into clean JSON error responses.

## 2.3 Why use layers? (interview gold)

- **Separation of concerns** — each layer has one job (web vs logic vs data).
- **Testability** — you can test the service without a web server or database.
- **Maintainability** — changing the database doesn't touch controllers.
- **Reusability** — multiple controllers can reuse the same service.

---

# PART 3 — END-TO-END REQUEST FLOW

## 3.1 Example A: Login (`POST /propNest/IAM/auth/login`)

1. Request arrives. **`JwtAuthenticationFilter`** sees the path is in `PUBLIC_PATHS`, so it **skips** token validation and lets it through.
2. **`AuthController.login()`** receives the JSON. `@Valid @RequestBody LoginRequest` converts JSON → object and validates it.
3. **`AuthService.login()`** runs inside a `@Transactional` method:
   - `userRepository.findByEmailIgnoreCase(email)` → loads user from **MySQL**.
   - `passwordEncoder.matches(rawPassword, storedHash)` → BCrypt verifies the password.
   - Checks the account status is `A` (Active).
   - Generates a random **refresh token**, saves a `UserSession` row.
   - `jwtService.generateAccessToken(user)` → mints a signed **JWT**.
   - `auditService.record(...)` → writes a `USER_LOGIN` row to `audit_log`.
4. Returns a `LoginResponse(accessToken, refreshToken, userId, role)` serialized to JSON with HTTP 200.

## 3.2 Example B: A protected admin call (`GET /IAM/admin/users`)

1. **`JwtAuthenticationFilter`** sees a protected `/IAM` path. It reads the `Authorization: Bearer <token>` header.
2. `jwtService.parseAccessToken(token)` validates the signature, issuer and expiry, and returns a `CurrentUser`.
3. The filter stores the `CurrentUser` in **`AuthContext`** (a `ThreadLocal`) for the duration of the request.
4. **`AdminUserController.getAllUsers()`** calls `accessGuard.requireAdmin()` → checks the role is `REAL_ESTATE_ADMIN`, else throws `AccessDeniedException` (403).
5. **`AdminUserService`** queries the repository and maps entities to `UserResponse` DTOs.
6. The filter's `finally` block calls `AuthContext.clear()` to avoid leaking identity across threads.

---

# PART 4 — FILE-BY-FILE WALKTHROUGH (CODE + MEANING)

## 4.1 The entry point — `PropNestApplication.java`

```java
@SpringBootApplication
public class PropNestApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropNestApplication.class, args);
    }
}
```

- **`@SpringBootApplication`** — a single annotation that combines three: `@Configuration` (this class can define beans), `@EnableAutoConfiguration` (Boot auto-configures based on the classpath), and `@ComponentScan` (scan this package and sub-packages for components).
- **`public static void main`** — the standard Java entry point; the JVM starts here.
- **`SpringApplication.run(...)`** — boots the Spring container, starts the embedded web server, and wires up all beans.

## 4.2 Configuration — `application.properties`

```properties
server.port=8083
server.servlet.context-path=/propNest
spring.datasource.url=jdbc:mysql://localhost:3306/propnest?createDatabaseIfNotExist=true
spring.jpa.hibernate.ddl-auto=update
propnest.jwt.secret=...   propnest.jwt.access-token-expiry-ms=1800000
```

- **`server.port`** — the port the app listens on (8083).
- **`server.servlet.context-path=/propNest`** — base URL prefix; every endpoint starts with `/propNest`.
- **`spring.datasource.*`** — how to connect to MySQL (URL, username, password, driver).
- **`createDatabaseIfNotExist=true`** — MySQL auto-creates the schema on first run.
- **`spring.jpa.hibernate.ddl-auto=update`** — Hibernate creates/updates tables to match the entities.
- **`spring.jpa.show-sql=true`** — print the generated SQL to the console (dev aid).
- **`spring.jpa.open-in-view=false`** — close the persistence context after the service layer (a best practice that avoids lazy-loading surprises).
- **`propnest.jwt.*`** — custom properties for the JWT secret, token expiry and issuer (read with `@Value`).

## 4.3 The Entity layer (database tables as classes)

### `User.java`

```java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "roleId", nullable = false)
    private Role role;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "passwordHash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 1)
    @Builder.Default
    private UserStatus status = UserStatus.A;
}
```

**Annotation meanings:**
- **`@Entity`** — marks this class as a JPA entity, i.e. it maps to a database table.
- **`@Table(name = "users")`** — the table name in MySQL.
- **`@Id`** — this field is the primary key.
- **`@GeneratedValue(strategy = GenerationType.IDENTITY)`** — the database auto-increments the id (MySQL AUTO_INCREMENT).
- **`@Column(name=..., nullable=false, unique=true, length=...)`** — maps the field to a column and sets constraints (NOT NULL, UNIQUE, max length).
- **`@ManyToOne`** — many users belong to one role (a relationship). `fetch = EAGER` means the role loads together with the user (needed for auth). `optional = false` means the role is mandatory.
- **`@JoinColumn(name = "roleId")`** — the foreign-key column linking `users` to `roles`.
- **`@Enumerated(EnumType.STRING)`** — store the enum by its **name** (`"A"`) not its numeric position (safer if the order changes).
- **`@Getter @Setter`** (Lombok) — generate all getters/setters at compile time.
- **`@NoArgsConstructor`** — generate a no-argument constructor (JPA requires one).
- **`@AllArgsConstructor`** — generate a constructor with all fields.
- **`@Builder`** — generate the builder pattern: `User.builder().email(...).build()`.
- **`@Builder.Default`** — keep the default value (`UserStatus.A`) when using the builder.

### `Role.java`
The 6 fixed roles. `roleId` is **not** auto-generated (`@Id` only) because the IDs are fixed by the API contract (1=OWNER … 6=REAL_ESTATE_ADMIN).

### `AuditLog.java`
Immutable record of a security action: `userId`, `action` (e.g. `USER_LOGIN`), `entityType`, `timeStamp` (a `java.time.Instant`).

### `UserSession.java`
Stores the **refresh token** so it can be revoked. Access tokens (JWT) are stateless and never stored; refresh tokens are stateful and persisted here with a `status` (`ACTIVE`/`REVOKED`) and an expiry time.

### Enums — `UserStatus` (A=Active, I=Inactive, S=Suspended) and `SessionStatus` (ACTIVE, REVOKED)
An **enum** is a fixed set of named constants — type-safe and self-documenting.

## 4.4 The Repository layer (data access without SQL)

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByStatus(UserStatus status);
    List<User> findByRole_RoleNameIgnoreCaseAndStatus(String roleName, UserStatus status);
}
```

- **`interface ... extends JpaRepository<User, Long>`** — you only declare an interface; Spring Data **generates the implementation** at runtime. `<User, Long>` = entity type and primary-key type.
- Inherited free methods: `save()`, `findById()`, `findAll()`, `existsById()`, `deleteById()`, etc.
- **Derived query methods** — Spring parses the **method name** and builds the query:
  - `findByEmailIgnoreCase` → `SELECT ... WHERE LOWER(email)=LOWER(?)`.
  - `existsByEmailIgnoreCase` → returns a boolean.
  - `findByRole_RoleNameIgnoreCaseAndStatus` → navigates into the related `Role` entity's `roleName` (the underscore `_` means "go into the Role object") **and** filters by status.
- **`Optional<User>`** — a null-safe container; avoids `NullPointerException`. You chain `.orElseThrow(...)`.

`AuditLogRepository` uses `findAllByOrderByTimeStampDesc()` — the `OrderBy...Desc` part adds an `ORDER BY timeStamp DESC`.

## 4.5 The DTO layer (request/response shapes)

DTOs are mostly **Java records** — immutable data carriers.

### Request DTO with validation — `RegisterRequest.java`

```java
public record RegisterRequest(
    @NotBlank(message = "name is required")
    @Size(max = 100) String name,
    @NotBlank @Email String email,
    @Pattern(regexp = "\\d{10,15}") String phone,
    @NotBlank @Size(min = 8, max = 100) String password,
    @NotNull Integer roleId
) {}
```

- **`record`** — a concise immutable class; auto-generates constructor, getters (`name()`), `equals`, `hashCode`, `toString`.
- **Bean Validation annotations** (from `jakarta.validation`):
  - **`@NotBlank`** — string must not be null/empty/whitespace.
  - **`@NotNull`** — value must not be null.
  - **`@Email`** — must be a valid email format.
  - **`@Size(min=, max=)`** — string length bounds.
  - **`@Pattern(regexp=)`** — must match the regex (here, 10–15 digits).
- These fire when the controller parameter is annotated with **`@Valid`**.

### Response DTO with a mapper — `UserResponse.java`

```java
public record UserResponse(Long userId, String name, String email, String phone, String role, String status) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail(),
            user.getPhone(), user.getRole().getRoleName(), user.getStatus().name());
    }
}
```

- The static **`from(User)`** factory maps an entity → DTO. Crucially, the **password hash is never copied**, so it can never leak to clients.

## 4.6 The Security layer

### `CurrentUser.java`
A record holding the authenticated caller: `userId`, `email`, `role`.

### `AuthContext.java` (ThreadLocal)

```java
public final class AuthContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();
    public static void set(CurrentUser u) { HOLDER.set(u); }
    public static void clear() { HOLDER.remove(); }
    public static CurrentUser require() {
        CurrentUser u = HOLDER.get();
        if (u == null) throw new TokenMissingException();
        return u;
    }
}
```

- **`ThreadLocal`** — gives each request thread its own copy of the current user. Because each HTTP request runs on its own thread, this safely carries identity without passing it through every method.
- **`final class` + `private` constructor** — a utility class that must never be instantiated.
- The filter calls `set()` at the start and `clear()` at the end (in a `finally`) so identity never leaks to the next request that reuses the thread.

### `JwtService.java` (the token engine)

```java
public String generateAccessToken(User user) {
    return Jwts.builder()
        .issuer(issuer)
        .subject(String.valueOf(user.getUserId()))
        .claim("email", user.getEmail())
        .claim("role", user.getRole().getRoleName())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(accessTokenExpiryMs)))
        .signWith(signingKey)
        .compact();
}
```

- **`@Service`** — a Spring-managed business component.
- **`@Value("${propnest.jwt.secret}")`** — injects the secret/expiry/issuer from `application.properties` into the constructor.
- **JWT (JSON Web Token)** = three parts: header.payload.signature. The payload carries **claims** (subject = userId, plus email and role).
- **`signWith(signingKey)`** — signs with an HMAC-SHA key derived from the Base64 secret, so the token cannot be tampered with.
- **`parseAccessToken`** verifies the signature, the issuer and the expiry; on failure it throws `InvalidTokenException`.
- **`generateRefreshToken`** uses `SecureRandom` to make a cryptographically random, opaque token (not a JWT). It is stored in the DB so it can be revoked.

### `JwtAuthenticationFilter.java` (the gatekeeper)

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        String path = req.getServletPath();
        if (!path.startsWith("/IAM") || PUBLIC_PATHS.contains(path)) { chain.doFilter(req, res); return; }
        String token = extractBearerToken(req);
        if (token == null) { writeUnauthorized(...); return; }
        try {
            CurrentUser user = jwtService.parseAccessToken(token);
            AuthContext.set(user);
            chain.doFilter(req, res);
        } catch (InvalidTokenException ex) { writeUnauthorized(...); }
        finally { AuthContext.clear(); }
    }
}
```

- **`@Component`** — Spring manages it; Boot auto-registers it as a servlet filter.
- **`extends OncePerRequestFilter`** — Spring base class guaranteeing the filter runs **once per request**.
- **`doFilterInternal`** — the method that runs for every request, **before** the controller.
- **Bearer token** — read from the `Authorization: Bearer <token>` header.
- Public paths (register/login/refresh) are skipped; anything else under `/IAM` needs a valid token.
- **`chain.doFilter(req, res)`** — pass the request along to the next filter / the controller.

### `AccessGuard.java` (authorization)

```java
@Component
public class AccessGuard {
    public CurrentUser requireAdmin() {
        CurrentUser user = AuthContext.require();
        if (!RoleNames.REAL_ESTATE_ADMIN.equals(user.role()))
            throw new AccessDeniedException("Access denied: REAL_ESTATE_ADMIN role required");
        return user;
    }
}
```

- Separates **authorization** (role check) from **authentication** (the filter). Admin controllers call `requireAdmin()` first.

### `SecurityBeansConfig.java`

```java
@Configuration
public class SecurityBeansConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```

- **`@Configuration`** — a class that defines beans.
- **`@Bean`** — registers the returned object in the Spring container so it can be injected anywhere.
- **`BCryptPasswordEncoder`** — hashes passwords with BCrypt (a slow, salted one-way hash). Plaintext passwords are never stored.

## 4.7 The Service layer (business logic)

### `AuthService.java` — the heart of authentication

Key methods (all `@Transactional`):
- **`register`** — validate role exists, check email is unique, hash the password, save the user, write an audit log.
- **`login`** — find user, verify password with `passwordEncoder.matches`, check status is Active, create a refresh-token session, mint an access token, audit it.
- **`logout`** — find the session by refresh token, set its status to `REVOKED`, audit it.
- **`refreshToken`** — validate the stored refresh token (active + not expired), mint a fresh access token.

```java
User user = userRepository.findByEmailIgnoreCase(request.email())
        .orElseThrow(InvalidCredentialsException::new);
if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
        throw new InvalidCredentialsException();
```

- **`.orElseThrow(InvalidCredentialsException::new)`** — if the `Optional` is empty, throw. `::new` is a **method reference** (shorthand for `() -> new InvalidCredentialsException()`).
- **`@Transactional`** — wraps the method in a database transaction: all DB writes commit together, or roll back together on error.

### Other services
- **`AdminUserService`** — create/list/get users, update status/role. Uses `@Transactional(readOnly = true)` for read methods (a performance hint: no dirty-checking, no flush).
- **`UserSelfService`** — get/update own profile, change password (verifies the current password first).
- **`RoleService`** — list the 6 roles (`findAll(Sort.by("roleId"))`).
- **`AuditService`** — `record(userId, action)` writes one audit row.
- **`AuditLogService`** — read audit logs (all, or per user).

## 4.8 The Controller layer (REST endpoints)

### `AuthController.java`

```java
@RestController
@RequestMapping("/IAM/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

**Annotation meanings:**
- **`@RestController`** — `@Controller` + `@ResponseBody`; every return value is serialized to JSON (not a view/HTML page).
- **`@RequestMapping("/IAM/auth")`** — base path for all methods in this controller.
- **`@PostMapping("/register")`** — maps HTTP **POST** `/IAM/auth/register` to this method. (Also `@GetMapping`, `@PutMapping`, etc.)
- **`@RequestBody`** — convert the incoming JSON body into a Java object.
- **`@Valid`** — trigger Bean Validation on that object; invalid input → 400 with messages.
- **`@PathVariable`** — bind a URL segment, e.g. `/{userId}` → method parameter.
- **`@RequestParam`** — bind a query parameter, e.g. `?role=OWNER`.
- **Constructor injection** — the controller receives its `AuthService` via the constructor (no `@Autowired` needed for a single constructor).
- **`ResponseEntity`** — lets you set the HTTP status (`CREATED` = 201, `ok` = 200) plus the body.

The admin controllers (`AdminUserController`, `RoleController`, `AuditLogController`) all call **`accessGuard.requireAdmin()`** before doing anything. `UserSelfController` reads the identity from `AuthContext.require().userId()` so a user can only ever act on their own account.

## 4.9 The Exception-handling layer

Two-tier design:

1. **`IamException`** — an `abstract` base class extending `RuntimeException`; each subclass carries the `HttpStatus` it maps to. Example: `InvalidCredentialsException` → 401.
2. **`IamExceptionHandler`** — `@RestControllerAdvice` with `@Order(HIGHEST_PRECEDENCE)`. It catches **any** `IamException` and builds a consistent `ErrorResponse` using the status the exception carries.
3. **`GlobalExceptionHandler`** — a fallback `@RestControllerAdvice` for everything else: validation errors (`MethodArgumentNotValidException` → 400), generic exceptions (→ 500), etc.

**Annotation meanings:**
- **`@RestControllerAdvice`** — global, cross-controller exception handling that returns JSON.
- **`@ExceptionHandler(SomeException.class)`** — the method that handles that exception type.
- **`@Order(Ordered.HIGHEST_PRECEDENCE)`** — make the IAM handler run before the generic one.

Why this matters: clients always get a clean, uniform error JSON (`status`, `message`, `path`, `timestamp`) instead of stack traces.

## 4.10 The Common layer
- **`ApiResponse<T>`** — a generic success wrapper (`success`, `message`, `data`, `timestamp`). `<T>` is a **generic type** so it can wrap any payload.
- **`ErrorResponse`** — the standard error body shape.

## 4.11 Startup seeding — `RoleSeeder.java`

```java
@Configuration
public class RoleSeeder {
    @Bean
    ApplicationRunner seedRoles(RoleRepository repo) {
        return args -> { for (Role r : DEFAULT_ROLES) if (!repo.existsById(r.getRoleId())) repo.save(r); };
    }
}
```

- **`ApplicationRunner`** — a Spring Boot hook that runs **once after startup**. Here it inserts the 6 fixed roles if they are missing, so the database is always ready.

---

# PART 5 — DATABASE SCHEMA

| Table | Key columns | Purpose |
|---|---|---|
| `users` | userId (PK), roleId (FK), name, email (unique), phone, passwordHash, status | Accounts. |
| `roles` | roleId (PK, fixed), roleName | The 6 roles. |
| `user_session` | sessionId (PK), userId, refreshToken, createdAt, refreshTokenExpiryTime, status | Refresh-token sessions. |
| `audit_log` | auditId (PK), userId, action, entityType, timeStamp | Security audit trail. |

**Relationship:** `users.roleId` → `roles.roleId` (Many-to-One). Created automatically by Hibernate from the entities (`ddl-auto=update`).

---

# PART 6 — COMPLETE ANNOTATION GLOSSARY (quick revision)

| Annotation | Layer | Meaning |
|---|---|---|
| `@SpringBootApplication` | Main | Config + auto-config + component scan. |
| `@Configuration` | Config | Class defines beans. |
| `@Bean` | Config | Registers a returned object as a Spring bean. |
| `@Component` | Any | Generic Spring-managed component. |
| `@Service` | Service | Marks a business-logic component. |
| `@RestController` | Controller | REST controller returning JSON. |
| `@RequestMapping` | Controller | Base URL path. |
| `@GetMapping`/`@PostMapping`/`@PutMapping` | Controller | Map an HTTP verb + path. |
| `@RequestBody` | Controller | JSON body → Java object. |
| `@PathVariable` | Controller | URL segment → parameter. |
| `@RequestParam` | Controller | Query parameter → parameter. |
| `@Valid` | Controller | Trigger Bean Validation. |
| `@NotBlank`/`@NotNull`/`@Email`/`@Size`/`@Pattern` | DTO | Validation rules. |
| `@Value` | Any | Inject a property value. |
| `@Transactional` | Service | Run in a DB transaction. |
| `@RestControllerAdvice` | Exception | Global JSON exception handling. |
| `@ExceptionHandler` | Exception | Handle a specific exception type. |
| `@Order` | Exception | Ordering of advices. |
| `@Entity` | Entity | Maps class to a DB table. |
| `@Table` | Entity | Table name. |
| `@Id` | Entity | Primary key. |
| `@GeneratedValue` | Entity | Auto-generate the key. |
| `@Column` | Entity | Column mapping + constraints. |
| `@ManyToOne` | Entity | Many-to-one relationship. |
| `@JoinColumn` | Entity | Foreign-key column. |
| `@Enumerated` | Entity | How to store an enum. |
| `@Getter`/`@Setter`/`@Builder`/`@NoArgsConstructor`/`@AllArgsConstructor` | Lombok | Generate boilerplate. |

---

# PART 7 — KEY CONCEPTS EXPLAINED (for confident answers)

- **Authentication vs Authorization** — AuthN = verifying identity (login). AuthZ = checking permissions (admin check).
- **JWT (access token)** — a self-contained, signed token. The server doesn't store it (stateless); it just verifies the signature. Lives 30 minutes here.
- **Refresh token** — a long-lived (7 days), opaque random string stored in the DB so it can be revoked. Used to get new access tokens without re-login.
- **Why two tokens?** — Short-lived access tokens limit damage if stolen; refresh tokens let users stay logged in and can be revoked on logout.
- **BCrypt** — a deliberately slow, salted, one-way hashing algorithm; the same password produces a different hash each time (the salt is built in). You never "decrypt" — you re-hash and compare with `matches()`.
- **RBAC (Role-Based Access Control)** — permissions are tied to roles (e.g. only `REAL_ESTATE_ADMIN`).
- **ORM / Hibernate** — maps Java objects to relational rows so you work with objects, not SQL.
- **IoC / Dependency Injection** — the Spring container creates objects and "injects" their dependencies, instead of you using `new`. We use **constructor injection**.
- **DTO vs Entity** — entities map to tables; DTOs are the API's request/response shapes. Keeping them separate avoids exposing internal fields (like the password hash).

---

# PART 8 — 100 INTERVIEW QUESTIONS WITH ANSWERS

## A. Core Java & Language (1–18)

**Q1. What is the difference between JDK, JRE and JVM?**
JVM runs bytecode; JRE = JVM + libraries to run apps; JDK = JRE + compiler/tools to build apps. This project targets JDK 21.

**Q2. What is a `record` in Java and why did you use it?**
A record is an immutable data class that auto-generates the constructor, getters, `equals`, `hashCode` and `toString`. Used for all DTOs (e.g. `LoginRequest`, `UserResponse`) because they are just data carriers.

**Q3. What is an `enum`? Where did you use it?**
A fixed set of named constants. Used for `UserStatus` (A/I/S) and `SessionStatus` (ACTIVE/REVOKED).

**Q4. What is `Optional` and why is it useful?**
A container that may or may not hold a value; it avoids `NullPointerException`. `userRepository.findByEmailIgnoreCase` returns `Optional<User>`, chained with `.orElseThrow(...)`.

**Q5. What is a lambda expression?**
A short anonymous function. Example: `.orElseThrow(() -> new InvalidRoleException(id))`.

**Q6. What is a method reference?**
A shorthand for a lambda that just calls a method/constructor: `InvalidCredentialsException::new`.

**Q7. What is the Streams API? Where is it used?**
A functional way to process collections. Used in services to map entities to DTOs: `users.stream().map(UserResponse::from).toList()`.

**Q8. What is the difference between `==` and `.equals()`?**
`==` compares references (same object); `.equals()` compares logical equality (e.g. string contents).

**Q9. What is the difference between checked and unchecked exceptions?**
Checked must be declared/caught (compile-time); unchecked extend `RuntimeException` (e.g. our `IamException`) and don't have to be declared.

**Q10. Why does your custom exception extend `RuntimeException`?**
So it propagates without `throws` clauses and is cleanly caught by the `@RestControllerAdvice`.

**Q11. What is `final`? Give an example from your project.**
`final` means cannot be reassigned. Service fields like `private final UserRepository userRepository;` are final because they're set once via the constructor.

**Q12. What is `ThreadLocal` and why did you use it?**
It gives each thread its own variable copy. `AuthContext` uses it to hold the current user per request, since each request runs on its own thread.

**Q13. Why must you clear a `ThreadLocal`?**
Threads are reused from a pool; if not cleared, one request's identity could leak into the next. The filter clears it in a `finally`.

**Q14. What is generics? Where used?**
Type-safe parameterization. `ApiResponse<T>`, `JpaRepository<User, Long>`, `Optional<User>`.

**Q15. What is the `java.time` API? Which class did you use?**
The modern date/time API. We use `Instant` for timestamps and token expiry.

**Q16. What is the builder pattern?**
A fluent way to construct objects step by step: `User.builder().email(...).build()`. Generated by Lombok's `@Builder`.

**Q17. What is immutability and why is it good?**
An object whose state cannot change after creation (like records). It's thread-safe and predictable.

**Q18. What is autoboxing?**
Automatic conversion between primitives and wrappers (e.g. `int` ↔ `Integer`). `roleId` is an `Integer` so it can be `null` before validation.

## B. OOP & Java Design (19–28)

**Q19. What are the four pillars of OOP?**
Encapsulation, Inheritance, Polymorphism, Abstraction.

**Q20. Show encapsulation in your project.**
Entity fields are `private` and accessed via Lombok getters/setters.

**Q21. Show inheritance in your project.**
`JwtAuthenticationFilter extends OncePerRequestFilter`; custom exceptions extend `IamException`; repositories extend `JpaRepository`.

**Q22. Show abstraction in your project.**
`IamException` is an `abstract` class; repositories are interfaces; `JwtService` hides JWT details behind simple methods.

**Q23. Show polymorphism in your project.**
The exception handler accepts `IamException` but receives any subclass at runtime and reads its specific status.

**Q24. What is an interface vs an abstract class?**
An interface is a pure contract (repositories); an abstract class can hold state and partial implementation (`IamException` holds the status).

**Q25. Why is constructor injection preferred over field injection?**
Dependencies become `final` and explicit, the class is easy to unit-test, and objects can't exist half-initialized.

**Q26. What is the Single Responsibility Principle? How does your layering show it?**
Each class has one reason to change. Controllers handle HTTP, services hold logic, repositories handle data.

**Q27. What is a DTO and why separate it from the entity?**
A Data Transfer Object is the API's data shape. Separating it hides internal fields (e.g. the password hash) and decouples the API from the DB.

**Q28. What design pattern does `JpaRepository` represent?**
The Repository (DAO) pattern — abstracting data access behind an interface.

## C. Spring Core / IoC / DI (29–42)

**Q29. What is the Spring IoC container?**
The part of Spring that creates, configures and manages objects (beans) and their dependencies.

**Q30. What is Dependency Injection?**
Supplying an object's dependencies from outside instead of creating them with `new`. Spring injects repositories into services, services into controllers.

**Q31. What is a Spring bean?**
An object managed by the Spring container, e.g. the `PasswordEncoder` defined with `@Bean`.

**Q32. Difference between `@Component`, `@Service`, `@Repository`, `@Controller`?**
All are stereotypes that create beans; they differ semantically. `@Service` = business logic, `@RestController` = web, `@Component` = generic (our filter and guard).

**Q33. What does `@Bean` do and where did you use it?**
It registers the returned object as a bean. Used for `passwordEncoder()` and the `ApplicationRunner` in `RoleSeeder`.

**Q34. What is `@Configuration`?**
Marks a class that defines beans via `@Bean` methods (`SecurityBeansConfig`, `RoleSeeder`).

**Q35. How does `@Value` work?**
It injects a value from configuration. `@Value("${propnest.jwt.secret}")` reads the JWT secret from `application.properties`.

**Q36. What are the bean scopes? What's the default?**
Singleton (default), prototype, request, session, etc. Our beans are singletons.

**Q37. What is `@Transactional`? What happens on an exception?**
It wraps a method in a DB transaction. On an unchecked exception, the transaction rolls back; otherwise it commits.

**Q38. What does `@Transactional(readOnly = true)` do?**
Hints that the method only reads — Hibernate can skip dirty-checking/flush for better performance. Used in all read services.

**Q39. Why don't your controllers use `@Autowired`?**
With a single constructor, Spring injects dependencies automatically; `@Autowired` is optional.

**Q40. What is the bean lifecycle in brief?**
Instantiate → inject dependencies → init callbacks → in use → destroy callbacks.

**Q41. What problem does IoC solve?**
It removes tight coupling — classes declare what they need, and the container wires it, making code testable and flexible.

**Q42. Can you inject an interface? How does Spring pick the implementation?**
Yes — Spring injects the matching bean. For repositories, Spring Data generates the implementation; for `PasswordEncoder`, we supply `BCryptPasswordEncoder` via `@Bean`.

## D. Spring Boot (43–52)

**Q43. What is Spring Boot and how is it different from Spring?**
Spring Boot is Spring plus auto-configuration, starters and an embedded server — far less manual setup.

**Q44. What does `@SpringBootApplication` combine?**
`@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`.

**Q45. What is auto-configuration?**
Boot configures beans automatically based on the classpath; seeing JPA + MySQL, it sets up the `DataSource` and Hibernate.

**Q46. What are starters? Name some you used.**
Curated dependency bundles: `spring-boot-starter-data-jpa`, `-validation`, `-webmvc`, `-webflux`.

**Q47. What is the embedded server?**
Boot ships an embedded Tomcat, so you run `main()` — no external server install. Our app runs on port 8083.

**Q48. How is configuration externalized?**
Via `application.properties` — DB URL, JWT secret, ports — separate from code.

**Q49. What is `server.servlet.context-path`?**
A global URL prefix; here `/propNest`, so login is `/propNest/IAM/auth/login`.

**Q50. What is `ApplicationRunner`? Where used?**
A hook that runs once after startup. `RoleSeeder` uses it to insert the 6 roles.

**Q51. How do you run/build the app with Maven?**
`./mvnw spring-boot:run` to run, `./mvnw clean package` to build the JAR.

**Q52. How do tests use a different database?**
Tests include H2 (in-memory) on the test classpath instead of MySQL, so they run fast and isolated.

## E. Spring MVC & REST (53–64)

**Q53. What is REST?**
An architectural style using HTTP verbs (GET/POST/PUT/DELETE) on resources, typically exchanging JSON.

**Q54. Difference between `@Controller` and `@RestController`?**
`@RestController` adds `@ResponseBody`, so methods return data (JSON) instead of view names.

**Q55. What does `@RequestBody` do?**
Deserializes the JSON request body into a Java object (via Jackson).

**Q56. What does `@PathVariable` vs `@RequestParam` do?**
`@PathVariable` binds a URL segment (`/users/{id}`); `@RequestParam` binds a query param (`?role=OWNER`). Both used in `AdminUserController`.

**Q57. What is `ResponseEntity`?**
A wrapper giving full control over status code, headers and body. We return 201 on create, 200 on success.

**Q58. How does input validation work here?**
DTOs carry `@NotBlank`, `@Email`, etc.; `@Valid` on the controller param triggers validation; failures become 400 via `GlobalExceptionHandler`.

**Q59. How are errors handled globally?**
`@RestControllerAdvice` classes catch exceptions and return a uniform `ErrorResponse`. `IamExceptionHandler` handles module exceptions first; `GlobalExceptionHandler` is the fallback.

**Q60. What HTTP status codes does your API use?**
200 OK, 201 Created, 400 Bad Request (validation), 401 Unauthorized, 403 Forbidden (not admin), 404 Not Found, 409 Conflict (duplicate email).

**Q61. What is content negotiation / how is JSON produced?**
Jackson (bundled by the web starter) serializes returned objects to JSON automatically.

**Q62. What is a servlet filter and why is it before the controller?**
A filter intercepts requests before they reach controllers. `JwtAuthenticationFilter` validates the token there so controllers can assume the caller is authenticated.

**Q63. What is `OncePerRequestFilter`?**
A Spring base class ensuring the filter executes exactly once per request, even with forwards/includes.

**Q64. How do you secure an endpoint for admins only?**
The controller calls `accessGuard.requireAdmin()`, which checks the role from `AuthContext` and throws 403 if not an admin.

## F. Spring Data JPA & Hibernate (65–77)

**Q65. What is JPA vs Hibernate?**
JPA is the specification (API); Hibernate is the implementation that actually maps objects to tables.

**Q66. What is ORM?**
Object-Relational Mapping — representing DB rows as Java objects so you avoid manual SQL.

**Q67. What does `JpaRepository` give you for free?**
CRUD and paging methods: `save`, `findById`, `findAll`, `existsById`, `deleteById`, etc.

**Q68. What are derived query methods?**
Methods whose name defines the query, e.g. `findByEmailIgnoreCase`. Spring Data generates the SQL from the name.

**Q69. Explain `findByRole_RoleNameIgnoreCaseAndStatus`.**
The `Role_RoleName` part navigates into the related `Role` entity's `roleName`; `IgnoreCase` makes it case-insensitive; `AndStatus` adds a second condition.

**Q70. What does `@Entity` and `@Table` do?**
`@Entity` marks the class as persistent; `@Table` sets the table name.

**Q71. What is `@Id` and `@GeneratedValue(IDENTITY)`?**
`@Id` is the primary key; `IDENTITY` delegates id generation to the DB's auto-increment.

**Q72. Why is `Role.roleId` not auto-generated?**
The 6 role IDs are fixed by the API contract, so they are assigned manually and seeded.

**Q73. What is `@ManyToOne` and `@JoinColumn`?**
Many users → one role; `@JoinColumn(name="roleId")` is the foreign-key column.

**Q74. What is the difference between EAGER and LAZY fetching?**
EAGER loads the relation immediately; LAZY loads on first access. `User.role` is EAGER because the role is always needed for auth.

**Q75. What does `@Enumerated(EnumType.STRING)` do and why not ORDINAL?**
Stores the enum name (`"A"`) instead of its position. STRING is safer — reordering the enum won't corrupt data.

**Q76. What does `ddl-auto=update` do?**
Hibernate creates/updates tables to match entities at startup. (In production you'd use migrations instead.)

**Q77. What is `open-in-view=false` and why set it?**
It closes the persistence context after the service layer, preventing accidental lazy-loading in the view and hidden N+1 queries.

## G. Security, JWT & BCrypt (78–89)

**Q78. What is the difference between authentication and authorization in your project?**
Authentication = the JWT filter validating the token; authorization = `AccessGuard` checking the role.

**Q79. What is a JWT and what are its three parts?**
A signed token: header, payload (claims), signature. Our payload holds userId (subject), email and role.

**Q80. Is your access token stored on the server? Why/why not?**
No — it's stateless. The server only verifies the signature and expiry, which scales well.

**Q81. What is a refresh token and why store it in the DB?**
A long-lived opaque token used to get new access tokens. It's stored so it can be revoked (logout) and checked for expiry.

**Q82. Why use two tokens instead of one?**
Short access tokens limit exposure if stolen; refresh tokens keep users logged in and are revocable.

**Q83. How is the JWT signed and verified?**
With an HMAC-SHA key from the Base64 secret. `parseAccessToken` verifies signature, issuer and expiry.

**Q84. What happens when an access token expires?**
`parseAccessToken` throws `InvalidTokenException`; the filter returns 401; the client calls `/refresh-token`.

**Q85. What is BCrypt and why use it over MD5/SHA?**
BCrypt is slow and salted, designed for passwords; MD5/SHA are fast and unsalted, so easier to brute-force.

**Q86. How do you verify a password if it's hashed?**
You never decrypt; you call `passwordEncoder.matches(raw, storedHash)`, which re-hashes and compares.

**Q87. Why is the login error message generic ("Invalid email or password")?**
To avoid leaking whether the email exists (prevents user enumeration).

**Q88. How does logout work if JWTs are stateless?**
You can't invalidate the JWT itself, but you revoke the refresh-token session, so no new access tokens can be minted.

**Q89. Why didn't you enable full Spring Security?**
The project only needed BCrypt + a custom JWT filter; full Spring Security auto-config was unnecessary complexity. This is documented in `SecurityBeansConfig`.

## H. Database / MySQL (90–93)

**Q90. What is a primary key vs a foreign key in your schema?**
PK uniquely identifies a row (`userId`); FK links tables (`users.roleId` → `roles.roleId`).

**Q91. How is the `email` uniqueness enforced?**
By `@Column(unique = true)` on the entity (a UNIQUE constraint) and an app check `existsByEmailIgnoreCase`.

**Q92. What is a connection pool and who provides it?**
A reusable set of DB connections; Spring Boot auto-configures HikariCP.

**Q93. How is the schema created?**
Hibernate generates it from the entities (`ddl-auto=update`), and the MySQL URL auto-creates the database.

## I. Project-specific & Scenario (94–100)

**Q94. Walk me through what happens on login, end to end.**
Filter skips the public path → controller validates `LoginRequest` → service finds the user, verifies the BCrypt password, checks status, creates a refresh session, mints a JWT, writes an audit log → returns tokens as JSON.

**Q95. How does a user only ever modify their own profile?**
`UserSelfController` takes the userId from `AuthContext` (the token), never from the URL, so a user can't act on someone else's account.

**Q96. How would you add a new admin-only endpoint?**
Add a controller method, call `accessGuard.requireAdmin()`, delegate to a service method, return a `ResponseEntity` with a DTO.

**Q97. How is auditing implemented and why does it matter?**
Every sensitive action calls `auditService.record(userId, action)`, writing to `audit_log`. It provides a security trail for compliance.

**Q98. What would you improve if this went to production?**
Move the JWT secret to a vault/env var, use Flyway/Liquibase instead of `ddl-auto=update`, add rate limiting on login, and add refresh-token rotation.

**Q99. How is your code testable?**
Constructor injection lets you mock repositories; services are tested in isolation, repositories with `@DataJpaTest` on H2, controllers at the web layer.

**Q100. Explain your project in two minutes (elevator pitch).**
PropNest's IAM module is a Spring Boot REST backend for identity and access management. It supports registration, JWT-based login with refresh tokens, role-based admin user management, self-service profile/password updates, and audit logging. It uses a clean layered architecture (controller → service → repository), Spring Data JPA over MySQL, BCrypt for passwords, a custom JWT authentication filter, and centralized exception handling — all built and run with Maven and Spring Boot.

---

*End of document — PropNest IAM Module Complete Guide.*
