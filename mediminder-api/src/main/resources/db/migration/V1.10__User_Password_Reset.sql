alter table "user" alter column verification_code drop not null;
alter table "user" drop constraint user_verification_code_key;
alter table "user" add column password_reset_code varchar(32) null;
