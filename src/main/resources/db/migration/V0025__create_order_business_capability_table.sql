CREATE SEQUENCE IF NOT EXISTS capability.order_business_capability_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS capability.order_business_capability
(
    id integer NOT NULL DEFAULT nextval('capability.order_business_capability_seq'),
    code character varying(50),
    name character varying(255)  NOT NULL,
    description text,
    owner character varying(255),
    created_date timestamp without time zone NOT NULL,
    last_modified_date timestamp without time zone,
    status character varying(255),
    parent_id integer,
    author character varying(255)  NOT NULL,
    mutable_bc_id integer,
    is_domain boolean NOT NULL,
    business_key text NOT NULL,
    CONSTRAINT pk_order_business_capability PRIMARY KEY (id),
    CONSTRAINT fk_order_bc FOREIGN KEY (mutable_bc_id)
    REFERENCES capability.business_capability (id) MATCH SIMPLE
                           ON UPDATE NO ACTION
                           ON DELETE NO ACTION,
    CONSTRAINT fk_order_business_capability FOREIGN KEY (parent_id)
    REFERENCES capability.business_capability (id) MATCH SIMPLE
                           ON UPDATE NO ACTION
                           ON DELETE NO ACTION
    );
