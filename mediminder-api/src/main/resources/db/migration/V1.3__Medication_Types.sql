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
values ('ORAL', 'Oral'),
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

alter table medication
    add column color varchar(16);