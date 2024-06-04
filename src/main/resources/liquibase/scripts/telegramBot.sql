-- liquibase formatted sql

-- changeset anton22582258:1
create TABLE notification_task (
    id serial8 primary key,
    chat_id int8 not null,
    time_and_date timeStamp not null,
    message text not null
)