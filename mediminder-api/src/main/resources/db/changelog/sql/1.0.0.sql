--liquibase formatted sql
--changeset g00glen00b:spring-batch

CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT ,
	JOB_NAME VARCHAR(100) NOT NULL,
	JOB_KEY VARCHAR(32) NOT NULL,
	constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT  ,
	JOB_INSTANCE_ID BIGINT NOT NULL,
	CREATE_TIME TIMESTAMP NOT NULL,
	START_TIME TIMESTAMP DEFAULT NULL ,
	END_TIME TIMESTAMP DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP,
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL ,
	PARAMETER_NAME VARCHAR(100) NOT NULL ,
	PARAMETER_TYPE VARCHAR(100) NOT NULL ,
	PARAMETER_VALUE VARCHAR(2500) ,
	IDENTIFYING CHAR(1) NOT NULL ,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	CREATE_TIME TIMESTAMP NOT NULL,
	START_TIME TIMESTAMP DEFAULT NULL ,
	END_TIME TIMESTAMP DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	COMMIT_COUNT BIGINT ,
	READ_COUNT BIGINT ,
	FILTER_COUNT BIGINT ,
	WRITE_COUNT BIGINT ,
	READ_SKIP_COUNT BIGINT ,
	WRITE_SKIP_COUNT BIGINT ,
	PROCESS_SKIP_COUNT BIGINT ,
	ROLLBACK_COUNT BIGINT ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP,
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
	references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
	JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_SEQ MAXVALUE 9223372036854775807 NO CYCLE;

--changeset g00glen00b:user

create table "user" (
    id uuid not null primary key,
    email varchar(128) not null unique,
    password varchar(128) not null,
    name varchar(128),
    timezone varchar(64),
    enabled boolean not null default false,
    admin boolean not null default false,
    verification_code varchar(32) not null unique
);

--changeset g00glen00b:medication

create table medication_type (
    id varchar(16) not null primary key,
    name varchar(128) not null unique
);

create table administration_type (
    id varchar(16) not null primary key,
    name varchar(128) not null unique
);

create table dose_type (
    id varchar(16) not null primary key,
    name varchar(128) not null unique
);

create table medication_type_administration_type (
    medication_type_id varchar(16) not null,
    administration_type_id varchar(16) not null,
    primary key(medication_type_id, administration_type_id),
    constraint fk_medication_type_administration_type_medication_type foreign key(medication_type_id) references medication_type(id),
    constraint fk_medication_type_administration_type_administration_type foreign key(administration_type_id) references administration_type(id)
);

create table medication_type_dose_type (
    medication_type_id varchar(16) not null,
    dose_type_id varchar(16) not null,
    primary key(medication_type_id, dose_type_id),
    constraint fk_medication_type_dose_type_medication_type foreign key(medication_type_id) references medication_type(id),
    constraint fk_medication_type_dose_type_dose_type foreign key(dose_type_id) references dose_type(id)
);

create table medication (
    id uuid not null primary key,
    user_id uuid not null,
    name varchar(128) not null,
    medication_type_id varchar(16) not null,
    administration_type_id varchar(16) not null,
    dose_type_id varchar(16) not null,
    doses_per_package decimal not null,
    constraint fk_medication_medication_type foreign key (medication_type_id) references medication_type(id),
    constraint fk_medication_administration_type foreign key (administration_type_id) references administration_type(id),
    constraint fk_medication_dose_type foreign key (dose_type_id) references dose_type(id)
);

--changeset g00glen00b:medication-type-values

insert into medication_type (id, name)
values ('SYRUP', 'Syrup'),
       ('TABLET', 'Tablet'),
       ('CAPSULE', 'Capsule'),
       ('CREAM', 'Cream'),
       ('LOTION', 'Lotion'),
       ('OINTMENT', 'Ointment'),
       ('SUPPOSITORY', 'Suppository'),
       ('DROP', 'Drop'),
       ('SPRAY', 'Spray'),
       ('INHALER', 'Inhaler'),
       ('INJECTION', 'Injection'),
       ('IMPLANT', 'Implant'),
       ('PATCH', 'Patch');

insert into administration_type (id, name)
values ('ORAL' , 'Oral'),
       ('SUBLINGUAL', 'Sublingual (Under the tongue)'),
       ('BUCCAL', 'Buccal (Between the gums and cheek)'),
       ('RECTAL', 'Rectal'),
       ('INTRAVENOUS', 'Intravenous (Into a vein)'),
       ('INTRAMUSCULAR', 'Intramuscular (Into a muscle)'),
       ('INTRATHECALLY', 'Intrathecally (Into the space around the spinal cord)'),
       ('SUBCUTANEOUS', 'Subcutaneous (Beneath the skin)'),
       ('INTRAARTERIAL', 'Intraarterial (Into an artery)'),
       ('TRANSNASAL', 'Transnasal'),
       ('INHALED', 'Inhaled'),
       ('VAGINAL', 'Vaginal'),
       ('TRANSDERMAL', 'Transdermal (Through the skin)'),
       ('INTRAOSSEOUS', 'Intraosseous (Into the bone)'),
       ('OCULAR', 'Ocular (Into the eye)'),
       ('OTIC', 'Otic (Into the ear)');

insert into dose_type (id, name)
values ('TABLET', 'tablet(s)'),
       ('CAPSULE', 'capsule(s)'),
       ('DOSE', 'dose(s)'),
       ('SUPPOSITORY', 'suppositorie(s)'),
       ('DROP', 'drop(s)'),
       ('SPRAY', 'spray(s)'),
       ('INHALE', 'inhale(s)'),
       ('INJECTION', 'injection(s)'),
       ('IMPLANT', 'implant(s)'),
       ('PATCH', 'patch(es)'),
       ('MILLILITER', 'milliliter');

insert into medication_type_administration_type (medication_type_id, administration_type_id)
values ('SYRUP', 'ORAL'),
       ('TABLET', 'ORAL'),
       ('TABLET', 'SUBLINGUAL'),
       ('TABLET', 'BUCCAL'),
       ('CAPSULE', 'ORAL'),
       ('CAPSULE', 'SUBLINGUAL'),
       ('CAPSULE', 'BUCCAL'),
       ('CREAM', 'VAGINAL'),
       ('CREAM', 'RECTAL'),
       ('CREAM', 'TRANSDERMAL'),
       ('LOTION', 'VAGINAL'),
       ('LOTION', 'RECTAL'),
       ('LOTION', 'TRANSDERMAL'),
       ('OINTMENT', 'VAGINAL'),
       ('OINTMENT', 'RECTAL'),
       ('OINTMENT', 'TRANSDERMAL'),
       ('SUPPOSITORY', 'RECTAL'),
       ('DROP', 'TRANSNASAL'),
       ('DROP', 'OCULAR'),
       ('DROP', 'OTIC'),
       ('SPRAY', 'TRANSDERMAL'),
       ('SPRAY', 'ORAL'),
       ('SPRAY', 'TRANSNASAL'),
       ('INHALER', 'INHALED'),
       ('INJECTION', 'INTRAVENOUS'),
       ('INJECTION', 'INTRAMUSCULAR'),
       ('INJECTION', 'INTRATHECALLY'),
       ('INJECTION', 'SUBCUTANEOUS'),
       ('INJECTION', 'INTRAARTERIAL'),
       ('INJECTION', 'INTRAOSSEOUS'),
       ('IMPLANT', 'TRANSDERMAL'),
       ('PATCH', 'TRANSDERMAL');

insert into medication_type_dose_type (medication_type_id, dose_type_id)
values ('SYRUP', 'MILLILITER'),
       ('SYRUP', 'DOSE'),
       ('TABLET', 'TABLET'),
       ('CAPSULE', 'CAPSULE'),
       ('CREAM', 'DOSE'),
       ('CREAM', 'MILLILITER'),
       ('LOTION', 'DOSE'),
       ('LOTION', 'MILLILITER'),
       ('OINTMENT', 'DOSE'),
       ('OINTMENT', 'MILLILITER'),
       ('SUPPOSITORY', 'SUPPOSITORY'),
       ('DROP', 'DROP'),
       ('DROP', 'MILLILITER'),
       ('SPRAY', 'SPRAY'),
       ('INHALER', 'INHALE'),
       ('INJECTION', 'INJECTION'),
       ('INJECTION', 'MILLILITER'),
       ('IMPLANT', 'IMPLANT'),
       ('PATCH', 'PATCH');

alter table medication add column color varchar(16);

--changeset g00glen00b:cabinet
create table cabinet_entry (
    id uuid not null primary key,
    user_id uuid not null,
    medication_id uuid not null,
    remaining_doses decimal not null,
    expiry_date date not null
);

--changeset g00glen00b:schedule
create table schedule (
    id uuid not null primary key,
    user_id uuid not null,
    medication_id uuid not null,
    starting_at date not null,
    ending_at_inclusive date,
    interval varchar(16) not null,
    time time without time zone not null,
    description varchar(128),
    dose decimal not null
);

create table completed_event (
    id uuid not null primary key,
    user_id uuid not null,
    schedule_id uuid not null,
    target_date timestamp without time zone not null,
    completed_date timestamp without time zone not null,
    dose decimal not null,
    constraint fk_completed_event_schedule foreign key (schedule_id) references schedule(id)
);

--changeset g00glen00b:notification
create type notification_type as enum ('INFO', 'WARNING');

create table notification (
    id uuid not null primary key,
    user_id uuid not null,
    reference varchar(128) not null,
    type notification_type not null,
    title varchar(64) not null,
    message varchar(256) not null,
    created_date timestamp default current_timestamp,
    constraint uq_notification_reference_user unique (user_id, reference)
);

create table subscription (
    user_id uuid not null primary key,
    endpoint varchar(256) not null,
    key varchar(256) not null,
    auth varchar(256) not null
);

--changeset g00glen00b:user-timezone-default
alter table "user" alter column timezone set default 'UTC';

--changeset g00glen00b:medication-color-enum
create type medication_color as enum ('RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'INDIGO', 'VIOLET', 'BLACK', 'WHITE', 'GRAY');

alter table medication drop column color;
alter table medication add column color medication_color;

--changeset g00glen00b:notification-active
alter table notification add column active boolean;

--changeset g00glen00b:milligram-dose
insert into dose_type (id, name) values ('MILLIGRAM', 'milligram');
insert into medication_type_dose_type (medication_type_id, dose_type_id) values ('INJECTION', 'MILLIGRAM');

--changeset g00glen00b:user-password-reset
alter table "user" alter column verification_code drop not null;
alter table "user" drop constraint user_verification_code_key;
alter table "user" add column password_reset_code varchar(32) null;

--changeset g00glen00b:token-reset
alter table "user" add column last_modified_date timestamp default current_timestamp;
update "user" set verification_code = null where enabled = true;

--changeset g00glen00b:notification-refactor
delete from notification;
alter table notification add column initiator_id uuid not null;
alter table notification add column delete_at timestamp not null;
alter type notification_type rename to notification_type_old;
create type notification_type as enum ('CABINET_ENTRY_EXPIRED', 'CABINET_ENTRY_ALMOST_EXPIRED', 'SCHEDULE_OUT_OF_DOSES', 'SCHEDULE_ALMOST_OUT_OF_DOSES', 'INTAKE_EVENT');
alter table notification alter column "type" type notification_type using "type"::text::notification_type;
drop type notification_type_old;
alter table notification drop column reference;
alter table notification drop column created_date;
