insert into medication_type (id, name) values ('POWDER', 'Powder');
insert into administration_type (id, name)
values ('FOOD', 'Mixed with food'),
       ('WATER', 'Dissolved in water');
insert into dose_type (id, name) values ('SACHET', 'sachet(s)');
insert into medication_type_administration_type (medication_type_id, administration_type_id)
values ('POWDER', 'FOOD'),
       ('POWDER', 'WATER');

insert into medication_type_dose_type (medication_type_id, dose_type_id)
values ('POWDER', 'SACHET'),
       ('POWDER', 'MILLIGRAM');
