
CREATE TABLE accounts (
                          id UUID PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          balance NUMERIC(19, 2) NOT NULL,
                          version BIGINT
);

CREATE TABLE transfers (
                           id UUID PRIMARY KEY,
                           from_account_id UUID NOT NULL,
                           to_account_id UUID NOT NULL,
                           amount NUMERIC(19, 2) NOT NULL,
                           status VARCHAR(30) NOT NULL,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                           completed_at TIMESTAMP WITH TIME ZONE,

                           CONSTRAINT fk_transfers_from_account
                               FOREIGN KEY (from_account_id)
                                   REFERENCES accounts (id),

                           CONSTRAINT fk_transfers_to_account
                               FOREIGN KEY (to_account_id)
                                   REFERENCES accounts (id),

                           CONSTRAINT chk_transfers_amount_positive
                               CHECK (amount > 0),

                           CONSTRAINT chk_transfers_different_accounts
                               CHECK (from_account_id <> to_account_id)
);

CREATE TABLE movements (
                           id UUID PRIMARY KEY,
                           account_id UUID NOT NULL,
                           type VARCHAR(30) NOT NULL,
                           amount NUMERIC(19, 2) NOT NULL,
                           transfer_id UUID NOT NULL,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                           CONSTRAINT fk_movements_account
                               FOREIGN KEY (account_id)
                                   REFERENCES accounts (id),

                           CONSTRAINT fk_movements_transfer
                               FOREIGN KEY (transfer_id)
                                   REFERENCES transfers (id),

                           CONSTRAINT chk_movements_amount_positive
                               CHECK (amount > 0)
);

CREATE TABLE notification_outbox (
                                     id UUID PRIMARY KEY,
                                     transfer_id UUID NOT NULL,
                                     account_id UUID NOT NULL,
                                     status VARCHAR(30) NOT NULL,
                                     type VARCHAR(50) NOT NULL,
                                     payload TEXT NOT NULL,
                                     attempts INTEGER NOT NULL,
                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     processed_at TIMESTAMP WITH TIME ZONE,
                                     next_attempt_at TIMESTAMP WITH TIME ZONE,
                                     error_message TEXT,

                                     CONSTRAINT fk_notification_outbox_transfer
                                         FOREIGN KEY (transfer_id)
                                             REFERENCES transfers (id),

                                     CONSTRAINT fk_notification_outbox_account
                                         FOREIGN KEY (account_id)
                                             REFERENCES accounts (id),

                                     CONSTRAINT chk_notification_outbox_attempts
                                         CHECK (attempts >= 0)
);

CREATE INDEX idx_movements_account_created_at
    ON movements (account_id, created_at DESC);

CREATE INDEX idx_movements_transfer_id
    ON movements (transfer_id);

CREATE INDEX idx_transfers_from_account_id
    ON transfers (from_account_id);

CREATE INDEX idx_transfers_to_account_id
    ON transfers (to_account_id);

CREATE INDEX idx_notification_outbox_status_next_attempt
    ON notification_outbox (status, next_attempt_at);

CREATE INDEX idx_notification_outbox_transfer_id
    ON notification_outbox (transfer_id);