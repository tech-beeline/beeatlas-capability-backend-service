/* Create Tables */

CREATE TABLE capability.business_capability
(
    id integer NOT NULL,
    code varchar(50) NULL,
    name varchar(255) NOT NULL,
    description varchar(255) NULL,
    owner varchar(255) NULL,
    created_date timestamp without time zone NOT NULL,
    last_modified_date timestamp without time zone NOT NULL,
    deleted_date timestamp without time zone NULL,
    status varchar(255) NULL,
    parent_id integer NULL,
    author varchar(255) NOT NULL,
    link varchar(255) NULL,
    is_domain boolean NOT NULL
)
;

CREATE TABLE capability.entity_type
(
    id integer NOT NULL,
    name varchar(20) NOT NULL
)
;

CREATE TABLE capability.find_name_sort_table
(
    id integer NOT NULL,
    vector text NOT NULL,
    type_id integer NOT NULL,
    id_ref integer NOT NULL
)
;

CREATE TABLE capability.tech_capability
(
    id integer NOT NULL,
    code varchar(50) NULL,
    name varchar(255) NOT NULL,
    description varchar(255) NULL,
    owner varchar(255) NULL,
    created_date timestamp without time zone NOT NULL,
    last_modified_date timestamp without time zone NOT NULL,
    deleted_date timestamp without time zone NULL,
    status varchar(255) NULL,
    author varchar(255) NOT NULL,
    link varchar(255) NULL
)
;

CREATE TABLE capability.tech_capability_relations
(
    id_rel integer NOT NULL,
    id_parent integer NOT NULL,
    id_child integer NOT NULL
)
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE capability.business_capability ADD CONSTRAINT pk_business_capability
    PRIMARY KEY (id)
;

ALTER TABLE capability.entity_type ADD CONSTRAINT pk_entity_type
    PRIMARY KEY (id)
;

ALTER TABLE capability.find_name_sort_table ADD CONSTRAINT pk_find_name_sort_table
    PRIMARY KEY (id)
;

ALTER TABLE capability.tech_capability ADD CONSTRAINT pk_tech_capability
    PRIMARY KEY (id)
;

ALTER TABLE capability.tech_capability_relations ADD CONSTRAINT pk_tech_capability_relations
    PRIMARY KEY (id_rel)
;

CREATE INDEX ixfk_business_capability ON capability.business_capability (parent_id ASC)
;

CREATE INDEX ixfk_find_name_sort_table_entity_type ON capability.find_name_sort_table (type_id ASC)
;

CREATE INDEX ixfk_tech_capability_relations_business_capability ON capability.tech_capability_relations (id_parent ASC)
;

CREATE INDEX ixfk_tech_capability_relations_tech_capability ON capability.tech_capability_relations (id_child ASC)
;

/* Create Foreign Key Constraints */

ALTER TABLE capability.business_capability ADD CONSTRAINT fk_business_capability
    FOREIGN KEY (parent_id) REFERENCES capability.business_capability (id) ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE capability.find_name_sort_table ADD CONSTRAINT fk_find_name_sort_table_entity_type
    FOREIGN KEY (type_id) REFERENCES capability.entity_type (id) ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE capability.tech_capability_relations ADD CONSTRAINT fk_tech_capability_relations_business_capability
    FOREIGN KEY (id_parent) REFERENCES capability.business_capability (id) ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE capability.tech_capability_relations ADD CONSTRAINT fk_tech_capability_relations_tech_capability
    FOREIGN KEY (id_child) REFERENCES capability.tech_capability (id) ON DELETE No Action ON UPDATE No Action
;

DROP SEQUENCE  IF EXISTS  tc_id_seq  CASCADE;
CREATE SEQUENCE tc_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;

DROP SEQUENCE  IF EXISTS  tcr_id_seq  CASCADE;
CREATE SEQUENCE tcr_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;

DROP SEQUENCE  IF EXISTS  capability.bc_id_seq  CASCADE;
CREATE SEQUENCE capability.bc_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
