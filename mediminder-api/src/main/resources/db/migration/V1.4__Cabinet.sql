create table cabinet_entry
(
    id              uuid    not null primary key,
    user_id         uuid    not null,
    medication_id   uuid    not null,
    remaining_doses decimal not null,
    expiry_date     date    not null
);