alter table "user" alter column id type varchar(64);
alter table medication alter column user_id type varchar(64);
alter table document alter column user_id type varchar(64);
alter table cabinet_entry alter column user_id type varchar(64);
alter table notification alter column user_id type varchar(64);
alter table subscription alter column user_id type varchar(64);
alter table completed_event alter column user_id type varchar(64);
alter table schedule alter column user_id type varchar(64);
