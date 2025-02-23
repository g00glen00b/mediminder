create type notification_type as enum ('INFO', 'WARNING');

create table notification
(
    id           uuid              not null primary key,
    user_id      uuid              not null,
    reference    varchar(128)      not null,
    type         notification_type not null,
    title        varchar(64)       not null,
    message      varchar(256)      not null,
    created_date timestamp default current_timestamp,
    constraint uq_notification_reference_user unique (user_id, reference)
);

create table subscription
(
    user_id  uuid         not null primary key,
    endpoint varchar(256) not null,
    key      varchar(256) not null,
    auth     varchar(256) not null
);