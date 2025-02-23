create type medication_color as enum ('RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'INDIGO', 'VIOLET', 'BLACK', 'WHITE', 'GRAY');

alter table medication drop column color;
alter table medication add column color medication_color;
