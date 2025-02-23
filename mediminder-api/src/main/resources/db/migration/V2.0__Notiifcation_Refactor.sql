delete from notification;
alter table notification add column initiator_id uuid not null;
alter table notification add column delete_at timestamp not null;
alter type notification_type rename to notification_type_old;
create type notification_type as enum ('CABINET_ENTRY_EXPIRED', 'CABINET_ENTRY_ALMOST_EXPIRED', 'SCHEDULE_OUT_OF_DOSES', 'SCHEDULE_ALMOST_OUT_OF_DOSES', 'INTAKE_EVENT');
alter table notification alter column "type" type notification_type using "type"::text::notification_type;
drop type notification_type_old;
alter table notification drop column reference;
alter table notification drop column created_date;