-- ============================================================
-- PropNest Module 2.2 — Run standalone (without IAM module)
-- ============================================================
-- The propnest_property schema was created with foreign keys that
-- require the IAM `users` table to already contain the referenced rows:
--   property.ownerId  -> users.userId   (fk_property_owner)
--   unit.tenantId     -> users.userId   (fk_unit_tenant)
--
-- Until IAM (Module 2.1) is integrated and `users` is populated, those
-- FKs make every createProperty / assignTenant call fail with:
--   "Cannot add or update a child row: a foreign key constraint fails ..."
--
-- This script drops ONLY the FKs that point at `users`, so the module
-- can run on its own. The intra-module FK unit.propertyId -> property
-- (fk_unit_property) is intentionally KEPT.
--
-- Run once:
--   mysql -uroot -proot propnest_property < sql/standalone_remove_iam_fks.sql
-- (or paste into MySQL Workbench against the propnest_property schema)
-- ============================================================

USE propnest_property;

-- Drop FK property.ownerId -> users.userId
ALTER TABLE property DROP FOREIGN KEY fk_property_owner;

-- Drop FK unit.tenantId -> users.userId
ALTER TABLE unit DROP FOREIGN KEY fk_unit_tenant;

-- Verify nothing else still references `users` (expect 0 rows):
SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users'
  AND TABLE_SCHEMA = 'propnest_property';

-- ============================================================
-- When IAM IS integrated later, re-add the constraints:
--   ALTER TABLE property ADD CONSTRAINT fk_property_owner
--     FOREIGN KEY (ownerId) REFERENCES users(userId)
--     ON DELETE RESTRICT ON UPDATE CASCADE;
--   ALTER TABLE unit ADD CONSTRAINT fk_unit_tenant
--     FOREIGN KEY (tenantId) REFERENCES users(userId)
--     ON DELETE RESTRICT ON UPDATE CASCADE;
-- ============================================================
