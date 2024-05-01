-- DROP SCHEMA public;

-- DROP SEQUENCE public.active_local_delivery_units_id_seq;

CREATE SEQUENCE public.active_local_delivery_units_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.active_local_delivery_units_id_seq OWNER TO licences;
GRANT ALL ON SEQUENCE public.active_local_delivery_units_id_seq TO licences;

-- DROP SEQUENCE public.audit_id_seq;

CREATE SEQUENCE public.audit_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.audit_id_seq OWNER TO licences;
GRANT ALL ON SEQUENCE public.audit_id_seq TO licences;

-- DROP SEQUENCE public.job_config_id_seq;

CREATE SEQUENCE public.job_config_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.job_config_id_seq OWNER TO licences;
GRANT ALL ON SEQUENCE public.job_config_id_seq TO licences;

-- DROP SEQUENCE public.knex_migrations_id_seq;

CREATE SEQUENCE public.knex_migrations_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.knex_migrations_id_seq OWNER TO licences;
GRANT ALL ON SEQUENCE public.knex_migrations_id_seq TO licences;

-- DROP SEQUENCE public.knex_migrations_lock_index_seq;

CREATE SEQUENCE public.knex_migrations_lock_index_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.knex_migrations_lock_index_seq OWNER TO licences;
GRANT ALL ON SEQUENCE public.knex_migrations_lock_index_seq TO licences;

-- DROP SEQUENCE public.licence_versions_id_seq;

CREATE SEQUENCE public.licence_versions_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.licence_versions_id_seq OWNER TO licences;
GRANT ALL ON SEQUENCE public.licence_versions_id_seq TO licences;

-- DROP SEQUENCE public.licences_id_seq;

CREATE SEQUENCE public.licences_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.licences_id_seq OWNER TO licences;
GRANT ALL ON SEQUENCE public.licences_id_seq TO licences;

-- DROP SEQUENCE public.notifications_config_id_seq;

CREATE SEQUENCE public.notifications_config_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.notifications_config_id_seq OWNER TO licences;
GRANT ALL ON SEQUENCE public.notifications_config_id_seq TO licences;

-- DROP SEQUENCE public.warnings_id_seq;

CREATE SEQUENCE public.warnings_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.warnings_id_seq OWNER TO licences;
GRANT ALL ON SEQUENCE public.warnings_id_seq TO licences;
-- public.active_local_delivery_units definition

-- Drop table

-- DROP TABLE public.active_local_delivery_units;

CREATE TABLE public.active_local_delivery_units (
                                                    id serial4 NOT NULL,
                                                    "timestamp" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                                    ldu_code varchar(10) NOT NULL,
                                                    probation_area_code varchar(255) NOT NULL,
                                                    CONSTRAINT active_local_delivery_units_pkey PRIMARY KEY (id)
);
CREATE INDEX ldu_code ON public.active_local_delivery_units USING btree (ldu_code);

-- Permissions

ALTER TABLE public.active_local_delivery_units OWNER TO licences;
GRANT ALL ON TABLE public.active_local_delivery_units TO licences;


-- public.audit definition

-- Drop table

-- DROP TABLE public.audit;

CREATE TABLE public.audit (
                              id serial4 NOT NULL,
                              "timestamp" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              "user" varchar(50) NOT NULL,
                              "action" varchar(50) NOT NULL,
                              details jsonb NULL,
                              CONSTRAINT audit_pkey PRIMARY KEY (id)
);
CREATE INDEX audit_by_booking_id ON public.audit USING btree (((details ->> 'bookingId'::text)));

-- Permissions

ALTER TABLE public.audit OWNER TO licences;
GRANT ALL ON TABLE public.audit TO licences;


-- public.job_config definition

-- Drop table

-- DROP TABLE public.job_config;

CREATE TABLE public.job_config (
                                   id serial4 NOT NULL,
                                   "name" varchar(255) NOT NULL,
                                   spec varchar(255) NOT NULL,
                                   CONSTRAINT job_config_name_unique UNIQUE (name),
                                   CONSTRAINT job_config_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.job_config OWNER TO licences;
GRANT ALL ON TABLE public.job_config TO licences;


-- public.knex_migrations definition

-- Drop table

-- DROP TABLE public.knex_migrations;

CREATE TABLE public.knex_migrations (
                                        id serial4 NOT NULL,
                                        "name" varchar(255) NULL,
                                        batch int4 NULL,
                                        migration_time timestamptz NULL,
                                        CONSTRAINT knex_migrations_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.knex_migrations OWNER TO licences;
GRANT ALL ON TABLE public.knex_migrations TO licences;


-- public.knex_migrations_lock definition

-- Drop table

-- DROP TABLE public.knex_migrations_lock;

CREATE TABLE public.knex_migrations_lock (
                                             "index" serial4 NOT NULL,
                                             is_locked int4 NULL,
                                             CONSTRAINT knex_migrations_lock_pkey PRIMARY KEY (index)
);

-- Permissions

ALTER TABLE public.knex_migrations_lock OWNER TO licences;
GRANT ALL ON TABLE public.knex_migrations_lock TO licences;


-- public.licence_versions definition

-- Drop table

-- DROP TABLE public.licence_versions;

CREATE TABLE public.licence_versions (
                                         id serial4 NOT NULL,
                                         "timestamp" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                         licence jsonb NULL,
                                         booking_id int4 NOT NULL,
                                         "version" int4 NOT NULL,
                                         "template" varchar(255) NOT NULL,
                                         vary_version int4 DEFAULT 0 NOT NULL,
                                         prison_number varchar(7) NULL,
                                         deleted_at timestamp NULL,
                                         CONSTRAINT licence_versions_pkey PRIMARY KEY (id)
);
CREATE INDEX licence_version_by_booking_id ON public.licence_versions USING btree (booking_id, version, id, template);
CREATE UNIQUE INDEX licence_versions_booking_id_version_vary_version_unique ON public.licence_versions USING btree (booking_id, version, vary_version) WHERE (deleted_at IS NULL);

-- Permissions

ALTER TABLE public.licence_versions OWNER TO licences;
GRANT ALL ON TABLE public.licence_versions TO licences;


-- public.licences definition

-- Drop table

-- DROP TABLE public.licences;

CREATE TABLE public.licences (
                                 id serial4 NOT NULL,
                                 licence jsonb NULL,
                                 booking_id int4 NOT NULL,
                                 stage varchar(255) NOT NULL,
                                 "version" int4 NOT NULL,
                                 transition_date timestamptz NULL,
                                 vary_version int4 DEFAULT 0 NOT NULL,
                                 additional_conditions_version int4 NULL,
                                 standard_conditions_version int4 NULL,
                                 prison_number varchar(7) NULL,
                                 deleted_at timestamp NULL,
                                 CONSTRAINT licences_pkey PRIMARY KEY (id)
);
CREATE INDEX licence_by_booking_id ON public.licences USING btree (booking_id, id, stage, version);

-- Permissions

ALTER TABLE public.licences OWNER TO licences;
GRANT ALL ON TABLE public.licences TO licences;


-- public.notifications_config definition

-- Drop table

-- DROP TABLE public.notifications_config;

CREATE TABLE public.notifications_config (
                                             id serial4 NOT NULL,
                                             email varchar(255) NOT NULL,
                                             establishment varchar(255) NOT NULL,
                                             "role" varchar(255) NOT NULL,
                                             "name" varchar(255) NULL,
                                             CONSTRAINT notifications_config_email_establishment_role_unique UNIQUE (email, establishment, role),
                                             CONSTRAINT notifications_config_pkey PRIMARY KEY (id)
);
CREATE INDEX mailbox_by_establishment ON public.notifications_config USING btree (establishment);
CREATE INDEX mailbox_by_role ON public.notifications_config USING btree (role);

-- Permissions

ALTER TABLE public.notifications_config OWNER TO licences;
GRANT ALL ON TABLE public.notifications_config TO licences;


-- public.staff_ids definition

-- Drop table

-- DROP TABLE public.staff_ids;

CREATE TABLE public.staff_ids (
                                  nomis_id varchar(255) NOT NULL,
                                  staff_id varchar(255) NOT NULL,
                                  first_name varchar(255) NULL,
                                  last_name varchar(255) NULL,
                                  organisation varchar(255) NULL,
                                  job_role varchar(255) NULL,
                                  email varchar(255) NULL,
                                  telephone varchar(255) NULL,
                                  org_email varchar(255) NULL,
                                  auth_onboarded bool DEFAULT false NOT NULL,
                                  deleted bool DEFAULT false NOT NULL,
                                  delius_username varchar(255) NULL,
                                  staff_identifier int8 NULL, -- Delius staff identifier (not staff code)
                                  CONSTRAINT pk_staff_id PRIMARY KEY (nomis_id)
);

-- Column comments

COMMENT ON COLUMN public.staff_ids.staff_identifier IS 'Delius staff identifier (not staff code)';

-- Permissions

ALTER TABLE public.staff_ids OWNER TO licences;
GRANT ALL ON TABLE public.staff_ids TO licences;


-- public.warnings definition

-- Drop table

-- DROP TABLE public.warnings;

CREATE TABLE public.warnings (
                                 id serial4 NOT NULL,
                                 "timestamp" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                 booking_id int4 NOT NULL,
                                 code varchar(255) NOT NULL,
                                 message varchar(255) NOT NULL,
                                 acknowledged bool NOT NULL,
                                 CONSTRAINT warnings_pkey PRIMARY KEY (id)
);
CREATE INDEX acknowledged ON public.warnings USING btree (acknowledged);
CREATE INDEX "bookingId" ON public.warnings USING btree (booking_id);
CREATE INDEX "timestamp" ON public.warnings USING btree ("timestamp");
CREATE UNIQUE INDEX unique_active_warning ON public.warnings USING btree (booking_id, code) WHERE (acknowledged = false);

-- Permissions

ALTER TABLE public.warnings OWNER TO licences;
GRANT ALL ON TABLE public.warnings TO licences;


-- public.v_licence_versions_excluding_deleted source

CREATE OR REPLACE VIEW public.v_licence_versions_excluding_deleted
AS SELECT licence_versions.id,
          licence_versions."timestamp",
          licence_versions.licence,
          licence_versions.booking_id,
          licence_versions.version,
          licence_versions.template,
          licence_versions.vary_version,
          licence_versions.prison_number,
          licence_versions.deleted_at
   FROM licence_versions
   WHERE licence_versions.deleted_at IS NULL;

-- Permissions

ALTER TABLE public.v_licence_versions_excluding_deleted OWNER TO licences;
GRANT ALL ON TABLE public.v_licence_versions_excluding_deleted TO licences;


-- public.v_licences_excluding_deleted source

CREATE OR REPLACE VIEW public.v_licences_excluding_deleted
AS SELECT licences.id,
          licences.licence,
          licences.booking_id,
          licences.stage,
          licences.version,
          licences.transition_date,
          licences.vary_version,
          licences.additional_conditions_version,
          licences.standard_conditions_version,
          licences.prison_number,
          licences.deleted_at
   FROM licences
   WHERE licences.deleted_at IS NULL;

-- Permissions

ALTER TABLE public.v_licences_excluding_deleted OWNER TO licences;
GRANT ALL ON TABLE public.v_licences_excluding_deleted TO licences;


-- public.v_staff_ids source

CREATE OR REPLACE VIEW public.v_staff_ids
AS SELECT staff_ids.nomis_id,
          staff_ids.staff_id,
          staff_ids.first_name,
          staff_ids.last_name,
          staff_ids.organisation,
          staff_ids.job_role,
          staff_ids.email,
          staff_ids.telephone,
          staff_ids.org_email,
          staff_ids.auth_onboarded,
          staff_ids.deleted,
          staff_ids.delius_username,
          staff_ids.staff_identifier
   FROM staff_ids
   WHERE staff_ids.deleted IS FALSE;

-- Permissions

ALTER TABLE public.v_staff_ids OWNER TO licences;
GRANT ALL ON TABLE public.v_staff_ids TO licences;




-- Permissions

GRANT ALL ON SCHEMA public TO pg_database_owner;
GRANT USAGE ON SCHEMA public TO public;
