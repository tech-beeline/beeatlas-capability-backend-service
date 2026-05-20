ALTER TABLE capability.enum_criterias
    ALTER COLUMN type DROP NOT NULL;

CREATE SEQUENCE IF NOT EXISTS capability.sequence_enum_criterias_id
    INCREMENT BY 1
    MINVALUE 1;

DO
$$
    DECLARE
        m INTEGER;
    BEGIN
        SELECT COALESCE(MAX(id), 0) INTO m FROM capability.enum_criterias;
        IF m = 0 THEN
            PERFORM setval('capability.sequence_enum_criterias_id', 1, false);
        ELSE
            PERFORM setval('capability.sequence_enum_criterias_id', m, true);
        END IF;
    END
$$;

ALTER TABLE capability.enum_criterias
    ALTER COLUMN id SET DEFAULT nextval('capability.sequence_enum_criterias_id'::regclass);

ALTER SEQUENCE capability.sequence_enum_criterias_id OWNED BY capability.enum_criterias.id;
