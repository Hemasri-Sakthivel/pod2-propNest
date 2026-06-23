# PropNest — Concepts Used (Java, Spring, Spring Boot, Spring Data JPA, MySQL, Maven)

> A theory-to-code map for your external review. For every concept you may be
> asked about, this document gives a **short definition** and the **exact place
> in PropNest where you used it**. Use the "Where in my project" column to point
> the reviewer at real code.

## 0. One-line project summary (say this first)

PropNest is a **Spring Boot REST backend** for an Identity & Access Management
(IAM) module. It exposes REST endpoints for **register / login / logout /
refresh-token**, **admin user management**, **role management**, and **audit
logs**. It uses **Spring Data JPA** over **MySQL** for persistence, **JWT** for
stateless authentication, **BCrypt** for password hashing, and is built with
**Maven**.

**Layered architecture used:**
`Controller (web)` → `Service (business logic)` → `Repository (data access)` → `MySQL`
with `DTO`s crossing the boundaries, `Entity`s mapped to tables, and a `JWT filter` + `AccessGuard` enforcing security.

---

# 1. Core Java Concepts

| Concept | What to say (theory) | Where in my project |
|---|---|---|
| **OOP — Encapsulation** | Fields are private, exposed via getters/setters. | `entity/User.java`, `entity/Role.java` (private fields + Lombok-generated accessors). |
| **Classes & Objects** | Blueprint vs instance. | Every `*Service`, `*Controller`, entity. |
| **Interfaces** | Contract without implementation; Spring injects an implementation. | All repositories are interfaces: `UserRepository`, `RoleRepository`, `AuditLogRepository`, `UserSessionRepository`. `PasswordEncoder` is an interface implemented by `BCryptPasswordEncoder`. |
| **Inheritance / extends** | Reusing a base class. | `JwtAuthenticationFilter extends OncePerRequestFilter`; repositories `extends JpaRepository`. |
| **Abstraction** | Hiding *how*, exposing *what*. | `JwtService` hides JWT signing details behind `generateAccessToken()` / `parseAccessToken()`. |
| **Enums** | Fixed set of constants. | `UserStatus` (A/I…), `SessionStatus` (ACTIVE/REVOKED). Used as a typed column via `@Enumerated`. |
| **Records (Java 16+)** | Immutable data carrier; auto getters/equals/hashCode. | All DTOs are records: `RegisterRequest`, `LoginRequest`, `LoginResponse`, `UserResponse`, etc. Access fields like `request.email()`. |
| **Generics** | Type-safe parameterization. | `JpaRepository<User, Long>`, `Optional<User>`, `List<User>`, `ResponseEntity<LoginResponse>`. |
| **Optional** | Null-safe container; avoids NullPointerException. | `userRepository.findByEmailIgnoreCase(...)` returns `Optional<User>`; chained with `.orElseThrow(...)`. |
| **Exception handling (custom exceptions)** | Throw domain-specific exceptions, handle centrally. | `InvalidCredentialsException`, `EmailAlreadyExistsException`, `AccountNotActiveException`, etc. in `exception/`. |
| **Lambda expressions** | Anonymous function. | `.orElseThrow(() -> new InvalidRoleException(...))`, `ifPresent(session -> {...})` in `AuthService`. |
| **Method references** | Shorthand for a lambda. | `.orElseThrow(InvalidCredentialsException::new)` in `AuthService.login()`. |
| **Streams API** | Functional pipeline over collections. | `GlobalExceptionHandler.handleValidation()` — `.stream().map(...).collect(Collectors.joining("; "))`. |
| **Static utility / private constructor** | Class that must not be instantiated. | `AuthContext` — `private AuthContext()` + static methods. |
| **ThreadLocal** | Per-thread storage (one value per request thread). | `AuthContext` holds the `CurrentUser` for the current request in a `ThreadLocal`. |
| **String / Base64 / SecureRandom** | Crypto-safe randomness + encoding. | `JwtService.generateRefreshToken()` uses `SecureRandom` + `Base64.getUrlEncoder()`. |
| **java.time (Instant)** | Modern date/time API. | `Instant.now()`, token expiry math in `JwtService` and `AuthService`. |
| **Builder pattern** | Fluent object construction. | `User.builder()...build()` in `AuthService.register()` (via Lombok `@Builder`). |

**Likely question — "Why records for DTOs?"**
Because DTOs are immutable data holders; records remove boilerplate (constructor,
getters, `equals`, `hashCode`) and make intent clear.

---

# 2. Lombok (boilerplate reduction — often grouped with "Java")

| Annotation | Purpose | Where |
|---|---|---|
| `@Getter` / `@Setter` | Generate accessors | `User`, `Role`, entities |
| `@NoArgsConstructor` / `@AllArgsConstructor` | Generate constructors (JPA needs no-args) | `User`, `Role` |
| `@Builder` / `@Builder.Default` | Builder pattern + default field value | `User` (`status = UserStatus.A` default) |

Configured as an **annotation processor** in `pom.xml` (`maven-compiler-plugin` → `annotationProcessorPaths`) and **excluded from the final jar** (`spring-boot-maven-plugin` excludes Lombok).

---

# 3. Spring Framework (Core) Concepts

| Concept | Theory | Where in my project |
|---|---|---|
| **IoC (Inversion of Control) / Spring Container** | Spring creates and manages objects (beans) instead of you using `new`. | The whole app — beans created at startup. |
| **Dependency Injection (Constructor injection)** | Dependencies passed in via constructor; promotes immutability + testability. | `AuthController(AuthService)`, `AuthService(UserRepository, RoleRepository, ...)` — all injected via constructor. |
| **Bean** | Object managed by Spring. | `PasswordEncoder` `@Bean` in `SecurityBeansConfig`; `ApplicationRunner` bean in `RoleSeeder`. |
| **Stereotype annotations** | Mark a class as a Spring-managed component. | `@Service` (`AuthService`, `JwtService`...), `@RestController` (controllers), `@Component` (`JwtAuthenticationFilter`, `AccessGuard`), `@Configuration` (`SecurityBeansConfig`, `RoleSeeder`). |
| **`@Configuration` + `@Bean`** | Java-based bean definitions. | `SecurityBeansConfig.passwordEncoder()`. |
| **`@Value` (property injection)** | Inject values from `application.properties`. | `JwtService` constructor — `@Value("${propnest.jwt.secret}")`, expiry, issuer. |
| **`@Transactional`** | Wrap a method in a DB transaction (commit/rollback). | `AuthService.register/login/logout/refreshToken` are `@Transactional`. |
| **Spring MVC (DispatcherServlet)** | Routes HTTP requests to controller methods. | All `@RestController` classes; `spring.main.web-application-type=servlet`. |
| **Servlet Filter / `OncePerRequestFilter`** | Cross-cutting logic per request, before controllers. | `JwtAuthenticationFilter` validates the Bearer token on every protected `/IAM/*` request. |

**Likely question — "Why constructor injection over `@Autowired` field injection?"**
It makes dependencies explicit and final, allows the class to be unit-tested
without Spring, and prevents partially-constructed objects.

---

# 4. Spring Boot Concepts

| Concept | Theory | Where in my project |
|---|---|---|
| **`@SpringBootApplication`** | Meta-annotation = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`. | `PropNestApplication.java`. |
| **Auto-configuration** | Boot auto-wires beans based on classpath (e.g. sees JPA + MySQL → configures `DataSource`/`EntityManager`). | DataSource & JPA configured automatically from `pom.xml` + `application.properties`. |
| **Starters** | Curated dependency bundles. | `spring-boot-starter-data-jpa`, `-validation`, `-webmvc`, `-webflux` in `pom.xml`. |
| **Embedded server** | No external Tomcat; runs `main()`. | `SpringApplication.run(...)` — runs on `server.port=8083`. |
| **Externalized configuration** | Settings in `application.properties`, not code. | `application.properties` — port, context-path, datasource, JPA, JWT secrets/expiry. |
| **`server.servlet.context-path`** | Base path prefix. | `/propNest` → endpoints look like `/propNest/IAM/auth/login`. |
| **`ApplicationRunner`** | Run code once after startup. | `RoleSeeder` seeds the 6 fixed roles on boot. |
| **Bean validation integration** | `@Valid` triggers automatic request validation. | Controllers: `@Valid @RequestBody RegisterRequest`. |
| **Profiles/test config** | Separate setup for tests. | Tests use H2 (`h2` test dependency) instead of MySQL. |

**Likely question — "What does Spring Boot actually do for you?"**
Auto-configuration + starters + embedded server + externalized config — you write
business code, Boot handles wiring, the servlet container, and sensible defaults.

---

# 5. Spring Web / REST Concepts

| Concept | Theory | Where in my project |
|---|---|---|
| **`@RestController`** | `@Controller` + `@ResponseBody`; returns JSON, not views. | `AuthController`, `AdminUserController`, `RoleController`, `AuditLogController`, `UserSelfController`. |
| **`@RequestMapping`** | Base path for a controller. | `@RequestMapping("/IAM/auth")` on `AuthController`. |
| **`@PostMapping` / `@GetMapping` / etc.** | Map HTTP verb + sub-path. | `@PostMapping("/login")`, `@PostMapping("/register")`. |
| **`@RequestBody`** | Deserialize JSON body → Java object. | `register(@Valid @RequestBody RegisterRequest request)`. |
| **`@PathVariable` / `@RequestParam`** | Bind URL parts / query params. | Admin/role controllers (e.g. user id in path). |
| **`ResponseEntity`** | Full control of status + body + headers. | `ResponseEntity.status(HttpStatus.CREATED).body(...)`, `ResponseEntity.ok(...)`. |
| **HTTP status codes** | Correct semantics (201 created, 200 ok, 401, 403, 404, 409). | `AuthController` (201 on register); `GlobalExceptionHandler` maps each exception to a status. |
| **`@RestControllerAdvice` + `@ExceptionHandler`** | Centralized, consistent error handling across all controllers. | `GlobalExceptionHandler` → uniform `ErrorResponse` JSON. |
| **DTO pattern** | Never expose entities directly; map to request/response objects. | `dto/` package — e.g. password is never returned, `LoginResponse` carries only token + role. |

**Likely question — "Difference between `@Controller` and `@RestController`?"**
`@RestController` = `@Controller` + `@ResponseBody`, so every method's return value
is serialized to the response body (JSON) instead of resolving a view name.

---

# 6. Security Concepts (custom, not full Spring Security)

> Important talking point: you deliberately **did not** enable full Spring
> Security auto-config. You pulled in only `spring-security-crypto` for BCrypt
> and built your own JWT filter. (See the comment in `SecurityBeansConfig`.)

| Concept | Theory | Where |
|---|---|---|
| **Authentication vs Authorization** | AuthN = who you are; AuthZ = what you can do. | AuthN: `JwtAuthenticationFilter` validates token. AuthZ: `AccessGuard.requireAdmin()` checks role. |
| **JWT (stateless access token)** | Signed token carrying claims; server doesn't store it. | `JwtService.generateAccessToken()` builds a signed JWT with `subject=userId`, `email`, `role` claims; `parseAccessToken()` verifies signature + issuer + expiry. |
| **Refresh token (stateful)** | Opaque random token stored in DB, used to mint new access tokens. | `JwtService.generateRefreshToken()` (SecureRandom) + persisted as `UserSession` in DB; validated in `AuthService.refreshToken()`. |
| **BCrypt password hashing** | One-way salted hash; never store plaintext. | `BCryptPasswordEncoder` bean; `passwordEncoder.encode()` on register, `.matches()` on login. |
| **Bearer token scheme** | `Authorization: Bearer <token>` header. | `JwtAuthenticationFilter.extractBearerToken()`. |
| **HMAC signing key** | Symmetric key signs/verifies the JWT. | `Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))`. |
| **Role-based access control (RBAC)** | Permissions tied to roles. | 6 fixed roles (`RoleSeeder`); `AccessGuard` enforces `REAL_ESTATE_ADMIN`. |
| **Audit logging** | Record security-relevant actions. | `AuditService.record(...)` on register/login/logout/refresh. |

---

# 7. Spring Data JPA Concepts

| Concept | Theory | Where in my project |
|---|---|---|
| **JPA / Hibernate (ORM)** | Map Java objects ↔ relational tables; Hibernate is the implementation. | All `@Entity` classes; `spring.jpa.*` properties. |
| **`@Entity` / `@Table`** | Mark a class as a persistent table. | `User` → `users`, `Role` → `roles`, `AuditLog`, `UserSession`. |
| **`@Id` + `@GeneratedValue`** | Primary key + generation strategy. | `User.userId` uses `GenerationType.IDENTITY` (DB auto-increment). `Role.roleId` is assigned manually (fixed IDs). |
| **`@Column`** | Map field → column with constraints. | `@Column(name="email", nullable=false, unique=true, length=150)`. |
| **`@Enumerated(EnumType.STRING)`** | Persist enum by name, not ordinal. | `User.status`, `UserSession.status`. |
| **Relationships — `@ManyToOne` + `@JoinColumn`** | Many users → one role; FK column. | `User.role` → `@ManyToOne(fetch=EAGER) @JoinColumn(name="roleId")`. |
| **Fetch strategy (EAGER/LAZY)** | When related data is loaded. | `User.role` is `FetchType.EAGER` (role always needed for auth). |
| **`JpaRepository<T, ID>`** | Generic CRUD + paging repository; no implementation needed. | `UserRepository extends JpaRepository<User, Long>`, etc. |
| **Derived query methods** | Method name → generated SQL. | `findByEmailIgnoreCase`, `existsByEmailIgnoreCase`, `findByStatus`, `findByRole_RoleNameIgnoreCaseAndStatus` (nested property + combined). |
| **Ordering in query names** | `OrderBy...Desc`. | `AuditLogRepository.findAllByOrderByTimeStampDesc()`, `findByUserIdOrderByTimeStampDesc`. |
| **CRUD via inherited methods** | `save`, `findById`, `existsById`. | `userRepository.save(user)`, `roleRepository.findById(...)`, `roleRepository.existsById(...)` in `RoleSeeder`. |
| **`@Transactional` boundaries** | Service-layer transactions. | `AuthService` methods. |
| **`ddl-auto=update`** | Hibernate creates/updates schema from entities. | `spring.jpa.hibernate.ddl-auto=update`. |
| **`open-in-view=false`** | Close the persistence context after the service layer (best practice). | `application.properties`. |
| **SQL logging** | Inspect generated SQL during dev. | `spring.jpa.show-sql=true`, `format_sql=true`. |

**Likely question — "How does `findByEmailIgnoreCase` work with no SQL?"**
Spring Data JPA parses the method name at startup and generates the query
(`WHERE lower(email) = lower(?)`) automatically — no implementation written.

---

# 8. MySQL Concepts

| Concept | Theory | Where in my project |
|---|---|---|
| **Relational database** | Data in tables with rows/columns + relationships. | `users`, `roles`, `audit_log`, `user_sessions` tables. |
| **JDBC driver / connection URL** | Java ↔ DB connectivity. | `mysql-connector-j`; `jdbc:mysql://localhost:3306/propnest`. |
| **DataSource / connection pool** | Reused DB connections (HikariCP via Boot). | Auto-configured from `spring.datasource.*`. |
| **Primary key + AUTO_INCREMENT** | Unique row identity. | `users.userId` (`GenerationType.IDENTITY`). |
| **Foreign key** | Referential integrity between tables. | `users.roleId` → `roles.roleId` (via `@JoinColumn`). |
| **Unique constraint** | No duplicate values. | `users.email` unique (`@Column(unique=true)`) — enforces one account per email. |
| **`createDatabaseIfNotExist`** | Auto-create schema on first run. | Datasource URL param. |
| **Seed data** | Pre-populate reference tables. | `RoleSeeder` inserts the 6 roles if absent. |

**Likely question — "How is the schema created?"**
Hibernate generates/updates it from the entities at startup (`ddl-auto=update`),
and the MySQL URL auto-creates the database if missing.

---

# 9. Maven Concepts

| Concept | Theory | Where in my project |
|---|---|---|
| **`pom.xml`** | Project Object Model — the build descriptor. | Project root `pom.xml`. |
| **`groupId` / `artifactId` / `version`** | Project coordinates. | `com.cog` / `propNest` / `0.0.1-SNAPSHOT`. |
| **Parent POM / dependency management** | Inherit managed versions. | `spring-boot-starter-parent` 4.0.6 — that's why most deps need no version. |
| **Dependencies & scopes** | What the build needs + when. | `runtime` (MySQL driver, jjwt-impl), `test` (H2, test starters), `optional` (Lombok). |
| **Transitive dependencies** | Pulled in automatically by starters. | Hibernate, Jackson, Tomcat all come via starters. |
| **Build plugins** | Extend the build lifecycle. | `spring-boot-maven-plugin` (repackage into runnable jar), `maven-compiler-plugin` (Java 21 + Lombok processor). |
| **Annotation processor path** | Lombok runs at compile time. | `maven-compiler-plugin` → `annotationProcessorPaths`. |
| **Java version property** | Target JDK. | `<java.version>21</java.version>`. |
| **Maven Wrapper (`mvnw`)** | Reproducible Maven without local install. | `.mvn/wrapper/maven-wrapper.properties`. |
| **Build lifecycle phases** | `compile` → `test` → `package`. | `mvn clean package` produces the runnable jar. |

**Likely question — "Why don't most dependencies have a `<version>`?"**
The `spring-boot-starter-parent` provides a managed dependency BOM, so versions
are inherited and kept compatible.

---

# 10. Testing Concepts (bonus — you have a full test suite)

| Concept | Theory | Where |
|---|---|---|
| **Unit tests** | Test one class in isolation. | `service/*Test.java`, `security/JwtServiceTest`, `AccessGuardTest`. |
| **`@DataJpaTest` / repository tests** | Slice test for the JPA layer on H2. | `repository/*Test.java`. |
| **Controller/web tests** | Test the web layer. | `controller/*Test.java`. |
| **In-memory DB for tests** | Fast, isolated DB. | H2 (`h2` test scope) instead of MySQL. |
| **Context load test** | Verify the app context starts. | `PropNestApplicationTests`. |

---

# 11. Request flow (great for a whiteboard answer)

**`POST /propNest/IAM/auth/login`**
1. `JwtAuthenticationFilter` — login is a **public path**, so it's skipped.
2. `AuthController.login()` — `@Valid` validates `LoginRequest`.
3. `AuthService.login()` (`@Transactional`):
   - `userRepository.findByEmailIgnoreCase()` → MySQL
   - `passwordEncoder.matches()` (BCrypt) verifies password
   - checks `UserStatus.A` (active)
   - creates a `UserSession` (refresh token) → saved to DB
   - `jwtService.generateAccessToken()` mints the JWT
   - `auditService.record(...)` logs the login
4. Returns `LoginResponse(accessToken, refreshToken, userId, role)` as JSON.

**Any protected call, e.g. admin endpoint:**
1. `JwtAuthenticationFilter` extracts Bearer token → `JwtService.parseAccessToken()` → binds `CurrentUser` to `AuthContext` (ThreadLocal).
2. `AccessGuard.requireAdmin()` checks the role → `403` if not admin.
3. Controller → Service → Repository → MySQL.
4. On error, `GlobalExceptionHandler` returns a consistent `ErrorResponse`.

---

# 12. Quick "I used X here" cheat sheet

- **Java records** → `dto/*`
- **Enums** → `UserStatus`, `SessionStatus`
- **Optional + orElseThrow** → `AuthService`, repositories
- **Streams** → `GlobalExceptionHandler`
- **ThreadLocal** → `AuthContext`
- **Constructor DI** → every service/controller
- **@Transactional** → `AuthService`
- **@RestControllerAdvice** → `GlobalExceptionHandler`
- **Bean validation (@Valid, @NotBlank, @Email…)** → `RegisterRequest` + controllers
- **JpaRepository + derived queries** → `UserRepository`, `AuditLogRepository`
- **@ManyToOne / @JoinColumn** → `User.role`
- **JWT + BCrypt** → `JwtService`, `SecurityBeansConfig`
- **ApplicationRunner seeding** → `RoleSeeder`
- **Maven starters / plugins** → `pom.xml`
- **MySQL datasource + Hibernate ddl** → `application.properties`
