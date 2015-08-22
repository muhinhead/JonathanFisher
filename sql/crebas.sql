CREAte table dbversion
(
    dbversion_id    int not null auto_increment,
    version_id      int not null,
    version         varchar(12),
    constraint dbversion_pk primary key (dbversion_id)
);

insert into dbversion values(1,1,'0.01');

create table author 
(
    author_id int not null auto_increment,
    name varchar(32) not null,
    constraint author_pk primary key (author_id)
);

create unique index author_uniq on author (name);

create table book
(
      book_id int not null auto_increment,
      isbn varchar(16) not null,
      name varchar(64) not null,
      author_id int not null,
      constraint book_pk primary key (book_id),
      constraint book_author_fk foreign key (author_id) references author (author_id)
);

