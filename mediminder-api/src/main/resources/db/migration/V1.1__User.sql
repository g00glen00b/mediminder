create table "user"
(
    id                uuid         not null primary key,
    email             varchar(128) not null unique,
    password          varchar(128) not null,
    name              varchar(128),
    timezone          varchar(64),
    enabled           boolean      not null default false,
    admin             boolean      not null default false,
    verification_code varchar(32)  not null unique
);