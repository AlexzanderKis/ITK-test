-- liquibase formatted sql

-- changeset Gordon_Freeman:1795009870907-1
CREATE TABLE wallets
(
    wallet_id UUID NOT NULL,
    balance   DECIMAL(14, 2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_wallets PRIMARY KEY (wallet_id),
    CONSTRAINT chk_wallets_balance_non_negative CHECK (balance>=0)
);
