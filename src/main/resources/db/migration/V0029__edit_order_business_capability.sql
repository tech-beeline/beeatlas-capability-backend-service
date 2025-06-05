ALTER TABLE capability.order_business_capability
    ADD COLUMN order_owner_id INT;

ALTER TABLE capability.order_business_capability
    ALTER COLUMN business_key DROP NOT NULL;