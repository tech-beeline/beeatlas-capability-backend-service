CREATE INDEX IF NOT EXISTS idx_business_capability_id_deleted_date ON business_capability (id, deleted_date);

CREATE INDEX IF NOT EXISTS idx_tech_capability_id_deleted_date ON tech_capability (id, deleted_date);