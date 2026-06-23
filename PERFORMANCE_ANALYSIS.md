# propNest — Tenant Onboarding & Lease

## tenant_application — GET response time by row count, with and without DB indexes

**Endpoint:** `GET /propNest/tenantOnboarding/getAllTenants` &nbsp;|&nbsp; `?status=A`

This is the index performance analysis for the `tenant_application` table, in the
same style as the CivicDesk IAM report. The numbers below are **measured by you**
by running the steps in *How to run* — fill the blank cells with the real values
from your machine (don't invent them).

---

## How to run (one row of the table at a time)

1. **Seed N rows** — run the standalone seeder with the row count as an argument
   (right-click `TenantApplicationDataSeeder` → Run, with program argument, e.g. `50000`):
   ```
   50000
   ```
   Do this once per row-count you want to measure (1000, 5000, 10000, 50000, 100000).

2. **WITHOUT index** — make sure there is no index on the filter column, then call
   the endpoint 3× and read the time from the app log line printed by `RequestTimingFilter`:
   ```
   GET /propNest/tenantOnboarding/getAllTenants?status=A -> 200 (2460 ms)
   ```
   (Postman also shows the time in the top-right of the response — either is fine.)
   Take the average of 3 calls.

3. **WITH index** — create the index (SQL below), then call the endpoint 3× again and
   average. Use `EXPLAIN` to confirm the index is actually used (see below).

4. **Improvement %** = `(NoIndex − WithIndex) / NoIndex × 100`.

> Tip: the filter logs milliseconds; the table uses seconds — divide by 1000.

---

## Index SQL (run in MySQL — database `propnest_tenant`)

```sql
USE propnest_tenant;

-- Helps the status filter:  WHERE status = ?
CREATE INDEX idx_ta_status ON tenant_application (status);

-- (optional) helps property/unit lookups if you filter on them later
CREATE INDEX idx_ta_property ON tenant_application (propertyId);

-- To measure the "No Index" rows, drop it again first:
-- DROP INDEX idx_ta_status ON tenant_application;

-- Prove the index is used (look for key = idx_ta_status, not NULL):
EXPLAIN SELECT * FROM tenant_application WHERE status = 'A';
```

---

## Results — Query: No filter (`getAllTenants`)

| Rows in Table | No Index (s) | With Index (s) | Improvement (%) |
|---------------|--------------|----------------|-----------------|
| 1,000         |              |                |                 |
| 5,000         |              |                |                 |
| 10,000        |              |                |                 |
| 50,000        |              |                |                 |
| 1,00,000      |              |                |                 |

> Note: a "no filter" full fetch scans the whole table either way, so the index
> improvement here is usually small (matches the CivicDesk report).

## Results — Query: Status filter (`getAllTenants?status=A`)

| Rows in Table | No Index (s) | With Index (s) | Improvement (%) |
|---------------|--------------|----------------|-----------------|
| 1,000         |              |                |                 |
| 5,000         |              |                |                 |
| 10,000        |              |                |                 |
| 50,000        |              |                |                 |
| 1,00,000      |              |                |                 |

> The status filter is where the `idx_ta_status` index pays off — expect the
> biggest improvement % here.

---

## What was added in code to support this

- **`RequestTimingFilter`** (`common/logging`) — logs `METHOD URI -> status (ms)`
  for every request, so each call's response time is captured in the app log.
- **Service logging** — `TenantApplicationService.getAllTenants` also logs the raw
  query time and row count (`Query returned N application(s) in X ms`).
- **`TenantApplicationDataSeeder`** — now takes a row-count argument so you can
  generate 1k / 5k / 10k / 50k / 100k datasets.
