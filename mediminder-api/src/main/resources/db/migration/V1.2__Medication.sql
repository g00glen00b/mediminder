create table medication_type
(
    id   varchar(16)  not null primary key,
    name varchar(128) not null unique
);

create table administration_type
(
    id   varchar(16)  not null primary key,
    name varchar(128) not null unique
);

create table dose_type
(
    id   varchar(16)  not null primary key,
    name varchar(128) not null unique
);

create table medication_type_administration_type
(
    medication_type_id     varchar(16) not null,
    administration_type_id varchar(16) not null,
    primary key (medication_type_id, administration_type_id),
    constraint fk_medication_type_administration_type_medication_type foreign key (medication_type_id) references medication_type (id),
    constraint fk_medication_type_administration_type_administration_type foreign key (administration_type_id) references administration_type (id)
);

create table medication_type_dose_type
(
    medication_type_id varchar(16) not null,
    dose_type_id       varchar(16) not null,
    primary key (medication_type_id, dose_type_id),
    constraint fk_medication_type_dose_type_medication_type foreign key (medication_type_id) references medication_type (id),
    constraint fk_medication_type_dose_type_dose_type foreign key (dose_type_id) references dose_type (id)
);

create table medication
(
    id                     uuid         not null primary key,
    user_id                uuid         not null,
    name                   varchar(128) not null,
    medication_type_id     varchar(16)  not null,
    administration_type_id varchar(16)  not null,
    dose_type_id           varchar(16)  not null,
    doses_per_package      decimal      not null,
    constraint fk_medication_medication_type foreign key (medication_type_id) references medication_type (id),
    constraint fk_medication_administration_type foreign key (administration_type_id) references administration_type (id),
    constraint fk_medication_dose_type foreign key (dose_type_id) references dose_type (id)
);