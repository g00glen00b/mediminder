alter table "user" add column last_modified_date timestamp default current_timestamp;
update "user" set verification_code = null where enabled = true;
