ALTER TABLE tc_group
DROP
CONSTRAINT IF EXISTS fk_tc_group_groups;

ALTER TABLE bc_group
DROP
CONSTRAINT IF EXISTS fk_bc_group_groups;

ALTER TABLE bc_group
    ADD CONSTRAINT fk_bc_group_groups
        FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE Cascade ON UPDATE No Action
;

ALTER TABLE tc_group
    ADD CONSTRAINT fk_tc_group_groups
        FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE Cascade ON UPDATE No Action
;