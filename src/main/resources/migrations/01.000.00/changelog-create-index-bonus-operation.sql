--liquibase formatted sql

--changeset e_pozdnyakova:01.000.00-create-index-bonus-operation
CREATE INDEX idx_bonus_operation_account_created
    ON bonus_operation (account_id, creation_at DESC);