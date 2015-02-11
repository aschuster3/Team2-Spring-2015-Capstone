# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table learner (
  id                        bigint not null,
  fullname                  varchar(255),
  owner_email               varchar(255),
  constraint pk_learner primary key (id))
;

create table session (
  id                        bigint not null,
  session_name              varchar(255),
  start_time                timestamp,
  end_time                  timestamp,
  constraint pk_session primary key (id))
;

create table unapproved_user (
  email                     varchar(255) not null,
  department                varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  token                     varchar(255),
  constraint pk_unapproved_user primary key (email))
;

create table user (
  email                     varchar(255) not null,
  password                  varchar(255),
  is_admin                  boolean,
  first_name                varchar(255),
  last_name                 varchar(255),
  constraint pk_user primary key (email))
;

create sequence learner_seq;

create sequence session_seq;

create sequence unapproved_user_seq;

create sequence user_seq;

alter table learner add constraint fk_learner_owner_1 foreign key (owner_email) references user (email) on delete restrict on update restrict;
create index ix_learner_owner_1 on learner (owner_email);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists learner;

drop table if exists session;

drop table if exists unapproved_user;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists learner_seq;

drop sequence if exists session_seq;

drop sequence if exists unapproved_user_seq;

drop sequence if exists user_seq;

