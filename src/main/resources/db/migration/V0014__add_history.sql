

DROP TABLE IF EXISTS history_busines_capability CASCADE
;

DROP TABLE IF EXISTS history_tech_capability CASCADE
;

DROP TABLE IF EXISTS history_tech_capability_relations CASCADE
;

/* Create Tables */

CREATE TABLE history_busines_capability
(
    id integer NOT NULL,
    id_ref integer NOT NULL,
    code varchar(50) NULL,
    name varchar(255) NOT NULL,
    description varchar(255) NULL,
    owner varchar(255) NULL,
    modified_date timestamp without time zone NOT NULL,
    status varchar(255) NULL,
    parent_id integer NULL,
    author varchar(255) NOT NULL,
    link varchar(255) NULL,
    is_domain boolean NOT NULL,
    version integer NOT NULL
)
;

CREATE TABLE history_tech_capability
(
    id integer NOT NULL,
    id_ref integer NOT NULL,
    code varchar(50) NULL,
    name varchar(255) NOT NULL,
    description varchar(255) NULL,
    owner varchar(255) NULL,
    modified_date timestamp without time zone NOT NULL,
    status varchar(255) NULL,
    author varchar(255) NOT NULL,
    link varchar(255) NULL,
    version integer NOT NULL
)
;

CREATE TABLE history_tech_capability_relations
(
    id integer NOT NULL,
    id_parent integer NOT NULL,
    id_history_child integer NOT NULL
)
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE history_busines_capability ADD CONSTRAINT pk_h_busines_capability
    PRIMARY KEY (id)
;

CREATE INDEX ixfk_h_busines_capability_busines_capability ON history_busines_capability (parent_id ASC)
;

ALTER TABLE history_tech_capability ADD CONSTRAINT pk_h_tech_capability
    PRIMARY KEY (id)
;

ALTER TABLE history_tech_capability_relations ADD CONSTRAINT pk_h_tech_capability_relations
    PRIMARY KEY (id)
;

CREATE INDEX ixfk_h_tech_capability_relations_busines_capability ON history_tech_capability_relations (id_parent ASC)
;

CREATE INDEX ixfk_h_tech_capability_relations_tech_capability ON history_tech_capability_relations (id_history_child ASC)
;

/* Create Foreign Key Constraints */

ALTER TABLE history_busines_capability ADD CONSTRAINT fk_h_busines_capability_busines_capability
    FOREIGN KEY (parent_id) REFERENCES business_capability (id) ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE history_busines_capability ADD CONSTRAINT fk_h_busines_capability_actual
    FOREIGN KEY (id_ref) REFERENCES business_capability (id) ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE history_tech_capability_relations ADD CONSTRAINT fk_h_tech_capability_relations_busines_capability
    FOREIGN KEY (id_parent) REFERENCES business_capability (id) ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE history_tech_capability_relations ADD CONSTRAINT fk_h_tech_capability_relations_tech_capability
    FOREIGN KEY (id_history_child) REFERENCES history_tech_capability (id) ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE history_tech_capability ADD CONSTRAINT fk_h_tech_capability_actual
    FOREIGN KEY (id_ref) REFERENCES tech_capability (id) ON DELETE No Action ON UPDATE No Action
;
