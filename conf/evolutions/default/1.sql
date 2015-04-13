# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table learner (
  email                     varchar(255) not null,
  uuid                      varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  owner_email               varchar(255),
  learner_type              varchar(255),
  constraint uq_learner_uuid unique (uuid),
  constraint pk_learner primary key (email))
;

create table recurring_session_group (
  id                        bigint not null,
  recurring_type            integer,
  constraint pk_recurring_session_group primary key (id))
;

create table schedule_template (
  title                     varchar(255) not null,
  constraint pk_schedule_template primary key (title))
;

create table session (
  id                        varchar(255) not null,
  title                     varchar(255),
  is_am                     boolean,
  date                      timestamp,
  physician                 varchar(255),
  assigned_learner          varchar(255),
  recurring_group_id        bigint,
  schedule_title            varchar(255),
  supported_learner_types_as_string varchar(500),
  constraint pk_session primary key (id))
;

create table session_template (
  id                        varchar(255) not null,
  title                     varchar(255),
  week                      integer,
  day                       integer,
  is_am                     boolean,
  schedule_title            varchar(255),
  constraint pk_session_template primary key (id))
;

create table unapproved_user (
  email                     varchar(255) not null,
  department                varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  phone_number              varchar(255),
  token                     varchar(255),
  constraint pk_unapproved_user primary key (email))
;

create table user (
  email                     varchar(255) not null,
  uuid                      varchar(255),
  password                  varchar(255),
  is_admin                  boolean,
  first_name                varchar(255),
  last_name                 varchar(255),
  phone_number              varchar(255),
  department                varchar(255),
  constraint uq_user_uuid unique (uuid),
  constraint pk_user primary key (email))
;

create table user_reset (
  user_email                varchar(255) not null,
  reset_token               varchar(255),
  constraint pk_user_reset primary key (user_email))
;

create sequence learner_seq;

create sequence recurring_session_group_seq;

create sequence schedule_template_seq;

create sequence session_seq;

create sequence session_template_seq;

create sequence unapproved_user_seq;

create sequence user_seq;

create sequence user_reset_seq;

alter table session_template add constraint fk_session_template_schedule_1 foreign key (schedule_title) references schedule_template (title) on delete restrict on update restrict;
create index ix_session_template_schedule_1 on session_template (schedule_title);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists learner;

drop table if exists recurring_session_group;

drop table if exists schedule_template;

drop table if exists session;

drop table if exists session_template;

drop table if exists unapproved_user;

drop table if exists user;

drop table if exists user_reset;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists learner_seq;

drop sequence if exists recurring_session_group_seq;

drop sequence if exists schedule_template_seq;

drop sequence if exists session_seq;

drop sequence if exists session_template_seq;

drop sequence if exists unapproved_user_seq;

drop sequence if exists user_seq;

drop sequence if exists user_reset_seq;

