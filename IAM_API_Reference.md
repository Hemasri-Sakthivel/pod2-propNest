# PropNest IAM — API Reference

> Every endpoint in the Identity & Access Management module, with sample request and response JSON. Use this for the "show me your APIs" part of the evaluation.

## Base URL & conventions

- **Base URL:** `http://localhost:8083/propNest`
- All IAM paths start with `/IAM`. Full example: `http://localhost:8083/propNest/IAM/auth/login`
- All bodies are **JSON** (`Content-Type: application/json`).
- Protected endpoints need the header: `Authorization: Bearer <accessToken>`

## Roles (seeded automatically at startup)

| roleId | roleName |
|---|---|
| 1 | OWNER |
| 2 | TENANT |
| 3 | PROPERTY_MANAGER |
| 4 | TECHNICIAN |
| 5 | FINANCE_EXECUTIVE |
| 6 | REAL_ESTATE_ADMIN |

## Access levels

| Symbol | Meaning |
|---|---|
| 🟢 Public | No token needed. |
| 🔵 Authenticated | Any valid access token. |
| 🔴 Admin | Valid token **and** role = REAL_ESTATE_ADMIN. |

---

# 1. Authentication — `/IAM/auth`

## 1.1 🟢 Register — `POST /IAM/auth/register`
Create a new account. (Register with `roleId: 6` to create your first admin for the demo.)

**Request body**
```json
{
  "name": "Asha Admin",
  "email": "admin@propnest.com",
  "phone": "9876543210",
  "password": "Admin@123",
  "roleId": 6
}
```
**Response — 201 Created**
```json
{ "message": "User registered successfully" }
```
**Errors:** 400 (validation), 409 (email already exists), 400 (invalid roleId).

## 1.2 🟢 Login — `POST /IAM/auth/login`
**Request body**
```json
{ "email": "admin@propnest.com", "password": "Admin@123" }
```
**Response — 200 OK**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6...",
  "refreshToken": "k3Jd9f-Qm2...urlsafe-random...",
  "userId": 1,
  "role": "REAL_ESTATE_ADMIN"
}
```
**Errors:** 401 (invalid email or password), 401 (account not active).

## 1.3 🟢 Refresh token — `POST /IAM/auth/refresh-token`
Exchange a valid refresh token for a new access token.

**Request body**
```json
{ "refreshToken": "k3Jd9f-Qm2...urlsafe-random..." }
```
**Response — 200 OK**
```json
{ "accessToken": "eyJhbGciOiJIUzI1NiJ9.newAccessToken..." }
```
**Errors:** 401 (invalid / expired / revoked refresh token).

## 1.4 🔵 Logout — `POST /IAM/auth/logout`
Revokes the refresh-token session. Needs an access token.

**Headers:** `Authorization: Bearer <accessToken>`
**Request body**
```json
{ "refreshToken": "k3Jd9f-Qm2...urlsafe-random..." }
```
**Response — 200 OK**
```json
{ "message": "Logged out successfully" }
```

---

# 2. Self-service — `/IAM/users/me`  (🔵 Authenticated)

The user id always comes from the token — never from the URL.

## 2.1 Get my profile — `GET /IAM/users/me`
**Response — 200 OK**
```json
{
  "userId": 1,
  "name": "Asha Admin",
  "email": "admin@propnest.com",
  "phone": "9876543210",
  "role": "REAL_ESTATE_ADMIN",
  "status": "A"
}
```

## 2.2 Update my profile — `PUT /IAM/users/me`
**Request body**
```json
{ "name": "Asha R", "phone": "9000000000" }
```
**Response — 200 OK**
```json
{ "message": "Profile updated successfully" }
```

## 2.3 Change my password — `PUT /IAM/users/me/password`
**Request body**
```json
{ "currentPassword": "Admin@123", "newPassword": "Admin@456" }
```
**Response — 200 OK**
```json
{ "message": "Password changed successfully" }
```
**Errors:** 401 (current password incorrect), 400 (newPassword too short).

---

# 3. Admin — user management — `/IAM/admin/users`  (🔴 Admin)

## 3.1 Create user — `POST /IAM/admin/users`
**Request body** (same shape as register)
```json
{
  "name": "Tenant One",
  "email": "tenant@propnest.com",
  "phone": "8888888888",
  "password": "Tenant@123",
  "roleId": 2
}
```
**Response — 201 Created**
```json
{ "message": "User created successfully" }
```

## 3.2 List users — `GET /IAM/admin/users`
Optional filters: `?role=TENANT&status=A` (both optional).

**Response — 200 OK**
```json
{
  "users": [
    { "userId": 1, "name": "Asha Admin", "email": "admin@propnest.com",
      "phone": "9876543210", "role": "REAL_ESTATE_ADMIN", "status": "A" },
    { "userId": 2, "name": "Tenant One", "email": "tenant@propnest.com",
      "phone": "8888888888", "role": "TENANT", "status": "A" }
  ]
}
```

## 3.3 Get user by id — `GET /IAM/admin/users/{userId}`
**Response — 200 OK** — a single `UserResponse` (same shape as 2.1). **Error:** 404 (user not found).

## 3.4 Update user status — `PUT /IAM/admin/users/{userId}/status`
Allowed status values: `A` (Active), `I` (Inactive), `S` (Suspended).

**Request body**
```json
{ "status": "S" }
```
**Response — 200 OK**
```json
{ "message": "User status updated successfully" }
```

## 3.5 Update user role — `PUT /IAM/admin/users/{userId}/role`
**Request body**
```json
{ "roleId": 3 }
```
**Response — 200 OK**
```json
{ "message": "User role updated successfully" }
```

---

# 4. Admin — roles — `GET /IAM/admin/roles`  (🔴 Admin)
**Response — 200 OK**
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

---

# 5. Admin — audit log — `/IAM/admin/audit-log`  (🔴 Admin)

## 5.1 All logs — `GET /IAM/admin/audit-log`
**Response — 200 OK**
```json
{
  "logs": [
    { "auditId": 5, "userId": 2, "action": "USER_CREATED",
      "entityType": "USER", "timeStamp": "2026-06-17T09:15:30Z" },
    { "auditId": 4, "userId": 1, "action": "USER_LOGIN",
      "entityType": "USER", "timeStamp": "2026-06-17T09:10:00Z" }
  ]
}
```

## 5.2 Logs for one user — `GET /IAM/admin/audit-log/user/{userId}`
Same shape, filtered to that user (newest first). **Error:** 404 (user not found).

---

# 6. Standard error response

Every error returns this shape (from `GlobalExceptionHandler` / `IamExceptionHandler`):
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "path": "/propNest/IAM/auth/login",
  "timestamp": "2026-06-17T09:10:00Z"
}
```

| Status | When it happens |
|---|---|
| 400 Bad Request | Validation failed (e.g. missing/invalid field). |
| 401 Unauthorized | No/invalid/expired token, or wrong credentials. |
| 403 Forbidden | Valid token but not an admin. |
| 404 Not Found | User id does not exist. |
| 409 Conflict | Email already registered. |

---

# 7. Endpoint summary

| # | Method | Path | Access |
|---|---|---|---|
| 1 | POST | /IAM/auth/register | 🟢 |
| 2 | POST | /IAM/auth/login | 🟢 |
| 3 | POST | /IAM/auth/refresh-token | 🟢 |
| 4 | POST | /IAM/auth/logout | 🔵 |
| 5 | GET | /IAM/users/me | 🔵 |
| 6 | PUT | /IAM/users/me | 🔵 |
| 7 | PUT | /IAM/users/me/password | 🔵 |
| 8 | POST | /IAM/admin/users | 🔴 |
| 9 | GET | /IAM/admin/users | 🔴 |
| 10 | GET | /IAM/admin/users/{userId} | 🔴 |
| 11 | PUT | /IAM/admin/users/{userId}/status | 🔴 |
| 12 | PUT | /IAM/admin/users/{userId}/role | 🔴 |
| 13 | GET | /IAM/admin/roles | 🔴 |
| 14 | GET | /IAM/admin/audit-log | 🔴 |
| 15 | GET | /IAM/admin/audit-log/user/{userId} | 🔴 |
