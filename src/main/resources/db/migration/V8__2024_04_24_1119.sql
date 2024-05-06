alter table role
alter COLUMN id drop IDENTITY;

alter table role
add column title VARCHAR(10);

alter table role
drop COLUMN role,
drop COLUMN created_by,
drop COLUMN created_on,
drop COLUMN modified_by,
drop COLUMN modified_on;

insert into role (id)
values (1); --GUEST

insert into role (id)
values (2); --JOB_SEEKER

insert into role (id)
values (3); --MENTOR

insert into role (id)
values (4); --ADMIN
