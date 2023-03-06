create table usr (
     id bigserial primary key,
     name varchar(255) not null,
     username varchar(160) not null,
     password varchar(150) not null,
     auth varchar(100) not null
)