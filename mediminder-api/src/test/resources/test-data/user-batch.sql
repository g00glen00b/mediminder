insert into "user" (id, email, password, name, timezone, enabled, admin, verification_code, password_reset_code, last_modified_date) values
    ('03479cd3-7e9a-4b79-8958-522cb1a16b1d', 'me1@example.org', '$2a$10$XaVU/nvIO4RpWzwvB/5Ev..CPrVJeNyumxY3ZJQip4wqdWnf/cbRm', 'User 1', 'UTC', true, true, null, null, '2024-06-29 10:00:00'),
    ('830257f6-9984-4bb6-9f2d-3710aade0b6a', 'me2@example.org', '$2a$10$yx08gjpuYtOPSmmT1csx9eA0BZu3CsG9xk5nZHmlqsPlbOOQ9Hofe', 'User 2', 'UTC', true, false, null, 'code1', '2024-06-29 10:00:00'),
    ('ae8861a8-2b6d-417b-93ae-7c7b34e2fb58', 'me6@example.org', '$2a$10$qOUW4oqn2PodrWhFGht7gObxSL1aIEpf8iv8BmM9ToRG2q0zpJKWe', 'User 3', 'UTC', true, false, null, 'code2', '2024-06-28 10:00:00'),
    ('44a9dc13-9549-4252-98d1-1bc84e31efcf', 'me3@example.org', '$2a$10$pGHaZntc0vwL/Ogyi.hxJOoTlksKRYLxI8lTMN4obWrqqQmVEoI3a', 'User 4', 'UTC', false, false, 'code3', null, '2024-06-29 10:00:00'),
    ('0f1f19c2-2d09-43b9-a7fc-ce82b9bfe43a', 'me4@example.org', '$2a$10$NSTAt6UbiBTTTh5K28/oOe3uKwOUI325Tgm.w69LHhGsdVTdL0Y4K', 'User 5', 'UTC', false, false, 'code4', null, '2024-06-28 10:00:00'),
    ('bbca513f-1a16-4233-bbb5-ab4076cca88f', 'me5@example.org', '$2a$10$xwSUx5xRZ.eaPKH/sw2zzOZwe/4sen0a7xSP6kR7OChmMM/sTxz7m', 'User 6', 'UTC', true, false, null, null, '2024-06-28 10:00:00');
