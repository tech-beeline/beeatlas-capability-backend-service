ALTER TABLE capability.tech_capability
    ADD COLUMN source TEXT DEFAULT 'SparxEA';

ALTER TABLE capability.business_capability
    ADD COLUMN source TEXT DEFAULT 'SparxEA';

ALTER TABLE capability.history_busines_capability
    ADD COLUMN source TEXT DEFAULT 'SparxEA';

ALTER TABLE capability.history_tech_capability
    ADD COLUMN source TEXT DEFAULT 'SparxEA';
