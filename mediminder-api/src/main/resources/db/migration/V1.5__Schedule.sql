create table schedule
(
    id                  uuid                   not null primary key,
    user_id             uuid                   not null,
    medication_id       uuid                   not null,
    starting_at         date                   not null,
    ending_at_inclusive date,
    interval            varchar(16)            not null,
    time                time without time zone not null,
    description         varchar(128),
    dose                decimal                not null
);

create table completed_event
(
    id             uuid                        not null primary key,
    user_id        uuid                        not null,
    schedule_id    uuid                        not null,
    target_date    timestamp without time zone not null,
    completed_date timestamp without time zone not null,
    dose           decimal                     not null,
    constraint fk_completed_event_schedule foreign key (schedule_id) references schedule (id)
);