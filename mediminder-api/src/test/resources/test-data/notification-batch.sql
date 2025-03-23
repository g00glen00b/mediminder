insert into "user" (id, email, password, name, timezone, enabled, admin, verification_code, password_reset_code, last_modified_date)
values ('03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'me1@example.org', '$2a$10$XaVU/nvIO4RpWzwvB/5Ev..CPrVJeNyumxY3ZJQip4wqdWnf/cbRm', 'User 1', 'UTC', true, true, null, null, '2025-03-18 10:00:00');

insert into medication (id, user_id, name, medication_type_id, administration_type_id, dose_type_id, doses_per_package, color)
values ('d0582490-195a-42f4-96e5-202bc7e8e30b', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'Dafalgan', 'TABLET', 'ORAL', 'TABLET', 100, 'RED'),
       ('cd7637ae-fda8-413a-a5e4-c0e1f0f68325', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'Hydrocortisone 14mg', 'CAPSULE', 'ORAL', 'CAPSULE', 60, 'YELLOW'),
       ('c266f875-0033-4cad-b96f-e17c37c81b66', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'Hydrocortisone 8mg', 'CAPSULE', 'ORAL', 'CAPSULE', 60, 'YELLOW');

insert into cabinet_entry (id, user_id, medication_id, remaining_doses, expiry_date)
values ('7e61721b-2d6d-49c4-b33f-c23ff74b1a70', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'd0582490-195a-42f4-96e5-202bc7e8e30b', 10, '2025-04-01'),
       ('b106ec0c-39e7-4328-a72c-f241e3026271', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'c266f875-0033-4cad-b96f-e17c37c81b66', 5, '2025-05-01'),
       ('abbde92d-3746-4ae7-abab-be9e8a6c80de', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'd0582490-195a-42f4-96e5-202bc7e8e30b', 20, '2025-02-25'),
       ('a7de7c65-eb68-4871-8f98-23d32bd1461f', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'd0582490-195a-42f4-96e5-202bc7e8e30b', 0, '2025-02-25'),
       ('922b8f73-cd63-4e59-ae89-6d8415e6ef0e', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'd0582490-195a-42f4-96e5-202bc7e8e30b', 20, '2025-02-27');

insert into schedule (id, user_id, medication_id, starting_at, ending_at_inclusive, interval, time, description, dose)
values ('61b1056e-66b2-4665-9d65-3469cb7b8ffe', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'd0582490-195a-42f4-96e5-202bc7e8e30b', '2025-01-01', null, 'P1D', '10:00', 'Before breakfast', 1),
       ('56c1db4e-7427-4eba-b3de-b7ea5d118b1c', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'cd7637ae-fda8-413a-a5e4-c0e1f0f68325', '2025-01-01', null, 'P1D', '10:30', 'After breakfast', 1),
       ('a5a85d19-7fc8-454a-883a-88ce836b9411', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'c266f875-0033-4cad-b96f-e17c37c81b66', '2025-01-01', null, 'P1D', '12:30', 'After lunch', 1);

insert into document (id, user_id, related_medication_id, content_type, expiry_date, filename, description)
values ('d1ccc34f-7fc3-4f65-b4da-8ae8ff0accf0', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', null, 'application/pdf', '2025-02-26', 'file1.pdf', 'Medical attest for Dafalgan'),
       ('691090bd-1142-4265-9fde-c2e744a282c1', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', null, 'application/pdf', '2025-02-27', 'file2.pdf', 'Medical attest for Hydrocortisone 14mg'),
       ('3e98dce5-5372-4e44-82db-45d649c89c61', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', null, 'application/pdf', '2025-03-12', 'file3.pdf', 'Medical attest for Hydrocortisone 8mg');

insert into notification (id, user_id, type, initiator_id, title, message, delete_at, active)
values ('2272b371-d6df-420e-b875-875321deb7e7', '03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'CABINET_ENTRY_EXPIRED', '7e61721b-2d6d-49c4-b33f-c23ff74b1a70', 'Cabinet entry notification', 'Cabinet entry notification message', '2025-01-01 10:00:00', true);

-- Public key generated through https://emn178.github.io/online-tools/ecdsa/key-generator/
-- SECG secp256r1 / X9.62 prime256v1 / NIST P-256
-- Base64 encoded
insert into subscription (user_id, endpoint, key, auth)
values ('03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'http://example.org/notification', 'BCMUqHuGnVEcZ0N4Uo+348R8mY5SNgr0EOCPFqZuquJkRbHn8239IqxaV7z5mEcQAOLRTIDAAqw8DqnleojoUBg=', 'test');