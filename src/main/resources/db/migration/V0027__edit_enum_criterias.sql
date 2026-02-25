ALTER TABLE capability.enum_criterias
    ADD COLUMN revers boolean NOT NULL DEFAULT false;

ALTER TABLE capability.enum_criterias
    ADD COLUMN max_desc text;

ALTER TABLE capability.enum_criterias
    ADD COLUMN min_desc text;