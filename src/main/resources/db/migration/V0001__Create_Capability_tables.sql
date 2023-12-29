/* Drop Tables */

DROP TABLE IF EXISTS capability.entity_type CASCADE
;

DROP TABLE IF EXISTS capability.find_name_sort_table CASCADE
;

DROP TABLE IF EXISTS capability.tech_capability CASCADE
;

DROP TABLE IF EXISTS capability.tech_capability_relations CASCADE
;

DROP TABLE IF EXISTS capability.business_capability CASCADE
;

/* Create Tables */

CREATE TABLE capability.business_capability
(
    id INTEGER PRIMARY KEY,
    code varchar(50) NULL,
    name varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    owner integer NULL,
    created_date timestamp without time zone NOT NULL,
    last_modified_date timestamp without time zone NOT NULL,
    deleted_date timestamp without time zone NULL,
    status varchar(255) NULL,
    parent_id integer NULL,
    author varchar(255) NOT NULL,
    link varchar(255) NULL,
    "isDomain" boolean NOT NULL
)
;

CREATE TABLE capability.entity_type
(
    id INTEGER PRIMARY KEY,
    name varchar(20) NOT NULL
)
;

CREATE TABLE capability.find_name_sort_table
(
    id INTEGER PRIMARY KEY,
    name varchar(255) NOT NULL,
    "typeId" integer NOT NULL,
    "idRef" integer NOT NULL
)
;

CREATE TABLE capability.tech_capability
(
    id INTEGER PRIMARY KEY,
    code varchar(50) NULL,
    name varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    owner integer NULL,
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
    "idRel" INTEGER PRIMARY KEY,
    "idParent" integer NOT NULL,
    "idChild" integer NOT NULL
)
;

/* Create Primary Keys, Indexes, Uniques, Checks */

CREATE INDEX "IXFK_Capability_UserProfile" ON capability.business_capability(owner ASC)
;

CREATE INDEX "IXFK_findNameSortTable_Domain" ON capability.find_name_sort_table ("idRef" ASC)
;

CREATE INDEX "IXFK_findNameSortTable_entityType" ON capability.find_name_sort_table ("typeId" ASC)
;

CREATE INDEX "nameIndex" ON capability.find_name_sort_table (name ASC)
;


CREATE INDEX "IXFK_TechCapability_UserProfile" ON capability.tech_capability (owner ASC)
;

CREATE INDEX "IXFK_TechCapabilityRelations_BusinesCapability" ON capability.tech_capability_relations ("idParent" ASC)
;

CREATE INDEX "IXFK_TechCapabilityRelations_TechCapability" ON capability.tech_capability_relations ("idChild" ASC)
;

/* Create Foreign Key Constraints */

ALTER TABLE capability.find_name_sort_table ADD CONSTRAINT "FK_findNameSortTable_BusinesCapability"
    FOREIGN KEY ("idRef") REFERENCES capability.business_capability (id) ON DELETE Cascade ON UPDATE No Action
;

ALTER TABLE capability.find_name_sort_table ADD CONSTRAINT "FK_findNameSortTable_entityType"
    FOREIGN KEY ("typeId") REFERENCES entity_type (id) ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE capability.find_name_sort_table ADD CONSTRAINT "FK_findNameSortTable_TechCapability"
    FOREIGN KEY ("idRef") REFERENCES capability.tech_capability (id) ON DELETE Cascade ON UPDATE No Action
;


ALTER TABLE capability.tech_capability_relations ADD CONSTRAINT "FK_TechCapabilityRelations_BusinesCapability"
    FOREIGN KEY ("idParent") REFERENCES capability.business_capability (id) ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE capability.tech_capability_relations ADD CONSTRAINT "FK_TechCapabilityRelations_TechCapability"
    FOREIGN KEY ("idChild") REFERENCES capability.tech_capability (id) ON DELETE No Action ON UPDATE No Action
;
