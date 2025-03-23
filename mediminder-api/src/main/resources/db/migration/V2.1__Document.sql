create table document
(
    id                    uuid         not null primary key,
    user_id               uuid         not null,
    related_medication_id uuid         null,
    content_type          varchar(64)  not null,
    expiry_date           date         null,
    filename              varchar(64)  not null,
    description           varchar(128) null
);