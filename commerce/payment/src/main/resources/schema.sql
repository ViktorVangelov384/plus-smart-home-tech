CREATE SCHEMA IF NOT EXISTS payment_schema;

CREATE TABLE IF NOT EXISTS payment_schema.payments
(
    payment_id UUID PRIMARY KEY,
    order_id UUID NOT NUll,
    delivery_total NUMERIC(19,2),
    total_payment NUMERIC(19,2),
    fee_total NUMERIC(19,2),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id
    ON payment_schema.payments(order_id);

CREATE INDEX IF NOT EXISTS idx_payments_status
    ON payment_schema.payments(status);