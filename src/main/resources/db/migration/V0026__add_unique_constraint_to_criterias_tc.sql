ALTER TABLE capability.criterias_tc
    ADD CONSTRAINT criterias_tc_tc_id_criterion_id_unique
        UNIQUE (tc_id, criterion_id);
