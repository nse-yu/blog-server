CREATE DATABASE IF NOT EXISTS sample;

USE sample;

create table hoge(
    id int not null auto_increment,
    name varchar(20) not null,
    primary key(id)
);

insert into hoge(name) values("nagase");