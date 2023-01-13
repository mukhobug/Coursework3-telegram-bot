-- liquibase formatted sql

-- changeset mukhobug:1
CREATE TABLE notification_task
(
    id                SERIAL PRIMARY KEY,
    chat_id           BIGINT       NOT NULL,
    notification_text VARCHAR(200) NOT NULL,
    date_to_send      TIMESTAMP    NOT NULL
);