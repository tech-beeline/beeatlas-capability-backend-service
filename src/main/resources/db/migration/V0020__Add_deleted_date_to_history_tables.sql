ALTER TABLE capability.history_busines_capability
    ADD COLUMN deleted_date timestamp without time zone DEFAULT NULL;

ALTER TABLE capability.history_tech_capability
    ADD COLUMN deleted_date timestamp without time zone DEFAULT NULL;
