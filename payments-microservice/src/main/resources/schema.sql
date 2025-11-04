CREATE TABLE IF NOT EXISTS payment (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    transaction_ref VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_payment_order_id ON payment (order_id);