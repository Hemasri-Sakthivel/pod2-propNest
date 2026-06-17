# PropNest — IAM API · Postman Testing Guide

Complete request/response reference and a **recommended testing order** for all 15
Identity & Access Management endpoints.

- **Base URL:** `http://localhost:8082/propNest/IAM`
- **Auth scheme:** `Authorization: Bearer <accessToken>` on every protected endpoint
- **Access token lifetime:** 30 minutes · **Refresh token lifetime:** 7 days
- **Content-Type:** `application/json` on every request that has a body

> Prerequisite: the app must be running (`mvnw spring-boot:run`) with MySQL up on
> `localhost:3306`. The 6 roles are auto-seeded on startup.

---

## 1. Postman setup (recommended)

### 1.1 Create an Environment with these variables

| Variable | Initial value |
|----------|---------------|
| `baseUrl` | `http://localhost:8082/propNest/IAM` |
| `adminToken` | *(leave empty — filled by tests)* |
| `adminRefreshToken` | *(empty)* |
| `userToken` | *(empty)* |
| `userRefreshToken` | *(empty)* |
| `tenantUserId` | *(empty)* |

### 1.2 Auto-capture tokens

On the **admin Login** request, add this to the **Scripts → Post-response** tab so
the token is stored automatically:

```javascript
const json = pm.response.json();
pm.environment.set("adminToken", json.accessToken);
pm.environment.set("adminRefreshToken", json.refreshToken);
```

On the **normal user Login** request use:

```javascript
const json = pm.response.json();
pm.environment.set("userToken", json.accessToken);
pm.environment.set("userRefreshToken", json.refreshToken);
```

Then in protected requests set the header `Authorization: Bearer {{adminToken}}`
(or `{{userToken}}`).

---

## 2. Recommended testing order

Roles exist at startup, but **no users do**. So you must register an admin first,
then everything else flows from the tokens you obtain.

| Step | Endpoint | Why this order |
|------|----------|----------------|
| 1 | `POST /auth/register` (roleId 6 = REAL_ESTATE_ADMIN) | Create the admin account |
| 2 | `POST /auth/login` (admin) | Get `adminToken` + `adminRefreshToken` |
| 3 | `GET /admin/roles` | Confirm seeded roles / get valid roleIds |
| 4 | `GET /users/me` | Read admin's own profile |
| 5 | `PUT /users/me` | Update admin name/phone |
| 6 | `POST /admin/users` (roleId 2 = TENANT) | Admin onboards a tenant → note its `userId` |
| 7 | `GET /admin/users` | List all users (try `?role=` / `?status=`) |
| 8 | `GET /admin/users/{userId}` | Read the tenant by id |
| 9 | `PUT /admin/users/{userId}/role` | Reassign tenant's role |
| 10 | `PUT /admin/users/{userId}/status` | Suspend / reactivate tenant |
| 11 | `POST /auth/register` (roleId 2) **or** login as the tenant | Get a non-admin `userToken` |
| 12 | `GET /admin/users` with `{{userToken}}` | **Expect 403** (role enforcement check) |
| 13 | `PUT /users/me/password` (as tenant) | Change own password |
| 14 | `POST /auth/login` (tenant, new password) | Confirm password change works |
| 15 | `POST /auth/refresh-token` | Mint a fresh access token |
| 16 | `GET /admin/audit-log` | See all recorded actions |
| 17 | `GET /admin/audit-log/user/{userId}` | See one user's actions |
| 18 | `POST /auth/logout` | Revoke the refresh token |
| 19 | `POST /auth/refresh-token` (same token) | **Expect 401** (token revoked) |

---

## 3. Endpoint reference

Legend: 🟢 public · 🟡 refresh-token only · 🔵 any authenticated user · 🔴 REAL_ESTATE_ADMIN only

---

### 1️⃣ 🟢 Register — `POST {{baseUrl}}/auth/register`

**Headers**
```
Content-Type: application/json
```
**Body**
```json
{
  "name": "Admin User",
  "email": "admin@propnest.com",
  "phone": "9876500000",
  "password": "Admin@123",
  "roleId": 6
}
```
**201 Created**
```json
{ "message": "User registered successfully" }
```
**Errors**
- `400` — invalid input (missing field, bad email, password < 8 chars, unknown roleId)
```json
{ "status": 400, "message": "email: email must be a valid address", "path": "/propNest/IAM/auth/register", "timestamp": "2026-06-16T10:30:00Z" }
```
- `409` — email already registered
```json
{ "status": 409, "message": "Email already registered", "path": "/propNest/IAM/auth/register", "timestamp": "2026-06-16T10:30:00Z" }
```

> `roleId` values: `1`=OWNER, `2`=TENANT, `3`=PROPERTY_MANAGER, `4`=TECHNICIAN, `5`=FINANCE_EXECUTIVE, `6`=REAL_ESTATE_ADMIN

---

### 2️⃣ 🟢 Login — `POST {{baseUrl}}/auth/login`

**Headers**
```
Content-Type: application/json
```
**Body**
```json
{
  "email": "admin@propnest.com",
  "password": "Admin@123"
}
```
**200 OK**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1Ni␣...",
  "refreshToken": "k3p9aZ...url-safe-random...",
  "userId": 1,
  "role": "REAL_ESTATE_ADMIN"
}
```
*(Add the Post-response script from §1.2 to store the tokens.)*

**Errors**
- `400` — missing/blank email or password
- `401` — wrong credentials, or account not active
```json
{ "status": 401, "message": "Invalid email or password", "path": "/propNest/IAM/auth/login", "timestamp": "..." }
```

---

### 3️⃣ 🔵 Logout — `POST {{baseUrl}}/auth/logout`

**Headers**
```
Content-Type: application/json
Authorization: Bearer {{adminToken}}
```
**Body**
```json
{ "refreshToken": "{{adminRefreshToken}}" }
```
**200 OK**
```json
{ "message": "Logged out successfully" }
```
**Errors**
- `401` — missing/expired access token
```json
{ "status": 401, "message": "Token missing or expired", "path": "/propNest/IAM/auth/logout", "timestamp": "..." }
```

---

### 4️⃣ 🟡 Refresh Token — `POST {{baseUrl}}/auth/refresh-token`

**Headers**
```
Content-Type: application/json
```
**Body**
```json
{ "refreshToken": "{{adminRefreshToken}}" }
```
**200 OK**
```json
{ "accessToken": "eyJhbGciOiJIUzI1Ni␣..." }
```
**Errors**
- `401` — refresh token expired, revoked (after logout) or unknown
```json
{ "status": 401, "message": "Refresh token expired or revoked", "path": "/propNest/IAM/auth/refresh-token", "timestamp": "..." }
```

---

### 5️⃣ 🔵 Get Own Profile — `GET {{baseUrl}}/users/me`

**Headers**
```
Authorization: Bearer {{adminToken}}
```
**Body:** *none*

**200 OK**
```json
{
  "userId": 1,
  "name": "Admin User",
  "email": "admin@propnest.com",
  "phone": "9876500000",
  "role": "REAL_ESTATE_ADMIN",
  "status": "A"
}
```
**Errors**
- `401` — missing/expired token

---

### 6️⃣ 🔵 Update Own Profile — `PUT {{baseUrl}}/users/me`

**Headers**
```
Content-Type: application/json
Authorization: Bearer {{adminToken}}
```
**Body**
```json
{
  "name": "Admin Updated",
  "phone": "9876511111"
}
```
**200 OK**
```json
{ "message": "Profile updated successfully" }
```
**Errors**
- `400` — blank name, or phone not 10–15 digits
- `401` — missing/expired token

> Only `name` and `phone` can be changed here. Email and role cannot.

---

### 7️⃣ 🔵 Change Own Password — `PUT {{baseUrl}}/users/me/password`

**Headers**
```
Content-Type: application/json
Authorization: Bearer {{userToken}}
```
**Body**
```json
{
  "currentPassword": "Tenant@123",
  "newPassword": "NewPass@456"
}
```
**200 OK**
```json
{ "message": "Password changed successfully" }
```
**Errors**
- `400` — current password is incorrect, or new password < 8 chars
```json
{ "status": 400, "message": "Current password is incorrect", "path": "/propNest/IAM/users/me/password", "timestamp": "..." }
```
- `401` — missing/expired token

---

### 8️⃣ 🔴 Admin Create User — `POST {{baseUrl}}/admin/users`

**Headers**
```
Content-Type: application/json
Authorization: Bearer {{adminToken}}
```
**Body**
```json
{
  "name": "Ravi Kumar",
  "email": "ravi@propnest.com",
  "phone": "9123456789",
  "password": "Tenant@123",
  "roleId": 2
}
```
**201 Created**
```json
{ "message": "User created successfully" }
```
**Errors**
- `400` — invalid input / unknown roleId
- `401` — missing/expired token
- `403` — caller is not REAL_ESTATE_ADMIN
- `409` — email already registered

> Tip: capture the created user's id by then calling `GET /admin/users?role=TENANT`
> and saving `tenantUserId` for the next steps.

---

### 9️⃣ 🔴 Get All Users — `GET {{baseUrl}}/admin/users`

**Headers**
```
Authorization: Bearer {{adminToken}}
```
**Optional query params**
```
?role=TENANT
?status=A
?role=TENANT&status=A
```
Example: `GET {{baseUrl}}/admin/users?role=TENANT&status=A`

**200 OK**
```json
{
  "users": [
    {
      "userId": 2,
      "name": "Ravi Kumar",
      "email": "ravi@propnest.com",
      "phone": "9123456789",
      "role": "TENANT",
      "status": "A"
    }
  ]
}
```
**Errors**
- `400` — invalid `status` value (allowed: `A`, `I`, `S`)
- `401` — missing/expired token
- `403` — not an admin

**Post-response script** to save the tenant id (when filtering to one user):
```javascript
const u = pm.response.json().users[0];
if (u) pm.environment.set("tenantUserId", u.userId);
```

---

### 🔟 🔴 Get User By Id — `GET {{baseUrl}}/admin/users/{{tenantUserId}}`

**Headers**
```
Authorization: Bearer {{adminToken}}
```
**200 OK**
```json
{
  "userId": 2,
  "name": "Ravi Kumar",
  "email": "ravi@propnest.com",
  "phone": "9123456789",
  "role": "TENANT",
  "status": "A"
}
```
**Errors**
- `401` — missing/expired token
- `403` — not an admin
- `404` — no user with that id
```json
{ "status": 404, "message": "User not found with id: 999", "path": "/propNest/IAM/admin/users/999", "timestamp": "..." }
```

---

### 1️⃣1️⃣ 🔴 Change User Status — `PUT {{baseUrl}}/admin/users/{{tenantUserId}}/status`

**Headers**
```
Content-Type: application/json
Authorization: Bearer {{adminToken}}
```
**Body**
```json
{ "status": "S" }
```
Allowed values: `A` = Active, `I` = Inactive (soft-delete), `S` = Suspended.

**200 OK**
```json
{ "message": "User status updated successfully" }
```
**Errors**
- `400` — invalid status value
```json
{ "status": 400, "message": "Invalid status value", "path": "/propNest/IAM/admin/users/2/status", "timestamp": "..." }
```
- `401` / `403` / `404`

> A suspended (`S`) or inactive (`I`) user can no longer log in (login returns 401).
> Set it back to `A` to re-enable login.

---

### 1️⃣2️⃣ 🔴 Reassign User Role — `PUT {{baseUrl}}/admin/users/{{tenantUserId}}/role`

**Headers**
```
Content-Type: application/json
Authorization: Bearer {{adminToken}}
```
**Body**
```json
{ "roleId": 3 }
```
**200 OK**
```json
{ "message": "User role updated successfully" }
```
**Errors**
- `400` — unknown roleId
```json
{ "status": 400, "message": "Invalid roleId", "path": "/propNest/IAM/admin/users/2/role", "timestamp": "..." }
```
- `401` / `403` / `404`

---

### 1️⃣3️⃣ 🔴 Get All Roles — `GET {{baseUrl}}/admin/roles`

**Headers**
```
Authorization: Bearer {{adminToken}}
```
**200 OK**
```json
{
  "roles": [
    { "roleId": 1, "roleName": "OWNER" },
    { "roleId": 2, "roleName": "TENANT" },
    { "roleId": 3, "roleName": "PROPERTY_MANAGER" },
    { "roleId": 4, "roleName": "TECHNICIAN" },
    { "roleId": 5, "roleName": "FINANCE_EXECUTIVE" },
    { "roleId": 6, "roleName": "REAL_ESTATE_ADMIN" }
  ]
}
```
**Errors**
- `401` — missing/expired token
- `403` — not an admin

---

### 1️⃣4️⃣ 🔴 Get All Audit Logs — `GET {{baseUrl}}/admin/audit-log`

**Headers**
```
Authorization: Bearer {{adminToken}}
```
**200 OK** (most recent first)
```json
{
  "logs": [
    { "auditId": 5, "userId": 2, "action": "USER_ROLE_UPDATED", "entityType": "USER", "timeStamp": "2026-06-16T10:40:12Z" },
    { "auditId": 1, "userId": 1, "action": "USER_REGISTERED",  "entityType": "USER", "timeStamp": "2026-06-16T10:30:00Z" }
  ]
}
```
**Errors**
- `401` / `403`

> Recorded actions include: `USER_REGISTERED`, `USER_LOGIN`, `USER_LOGOUT`,
> `TOKEN_REFRESHED`, `PROFILE_UPDATED`, `PASSWORD_CHANGED`, `USER_CREATED`,
> `USER_STATUS_UPDATED`, `USER_ROLE_UPDATED`.

---

### 1️⃣5️⃣ 🔴 Get Audit Logs For A User — `GET {{baseUrl}}/admin/audit-log/user/{{tenantUserId}}`

**Headers**
```
Authorization: Bearer {{adminToken}}
```
**200 OK**
```json
{
  "logs": [
    { "auditId": 5, "userId": 2, "action": "USER_ROLE_UPDATED",   "entityType": "USER", "timeStamp": "2026-06-16T10:40:12Z" },
    { "auditId": 3, "userId": 2, "action": "USER_CREATED",        "entityType": "USER", "timeStamp": "2026-06-16T10:35:00Z" }
  ]
}
```
**Errors**
- `401` / `403`
- `404` — no user with that id

---

## 4. Quick error-code cheat sheet

| Code | Meaning | Typical trigger |
|------|---------|-----------------|
| 200 | OK | Successful GET / PUT / login / refresh / logout |
| 201 | Created | register, admin create user |
| 400 | Bad Request | validation failure, invalid roleId, invalid status, wrong current password |
| 401 | Unauthorized | missing/expired/invalid access token, bad login credentials, revoked refresh token |
| 403 | Forbidden | authenticated but not REAL_ESTATE_ADMIN on an admin endpoint |
| 404 | Not Found | unknown userId on admin user / audit-log lookups |
| 409 | Conflict | duplicate email |
| 500 | Internal Server Error | unexpected server fault |

All error bodies share this shape:
```json
{ "status": 401, "message": "...", "path": "/propNest/IAM/...", "timestamp": "2026-06-16T10:30:00Z" }
```

---

## 5. Negative tests worth running

| Scenario | How | Expected |
|----------|-----|----------|
| No token on protected endpoint | `GET /users/me` with no `Authorization` header | `401 Token missing or expired` |
| Tampered/expired access token | Change one char of `{{adminToken}}` | `401 Invalid access token` |
| Non-admin hits admin endpoint | `GET /admin/users` with `{{userToken}}` | `403 Forbidden` |
| Duplicate registration | Register same email twice | `409 Conflict` |
| Login while suspended | Set status `S`, then login | `401 Account is not active` |
| Refresh after logout | Logout, then refresh same token | `401 Refresh token expired or revoked` |
| Invalid roleId on register | `"roleId": 99` | `400 Invalid roleId` |
| Invalid status value | `PUT .../status` body `{ "status": "X" }` | `400 Invalid status value` |
