CREATE SCHEMA IF NOT EXISTS delivery_schema;

CREATE TABLE IF NOT EXISTS delivery_schema.deliveries
(
    delivery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    delivery_state VARCHAR(100) NOT NULL,
    country_from VARCHAR(50),
    city_from VARCHAR(100),
    street_from VARCHAR(100),
    house_from VARCHAR(100),
    flat_from VARCHAR(100),
    country_to VARCHAR(100),
    city_to VARCHAR(100),
    street_to VARCHAR(100),
    house_to VARCHAR(100),
    flat_to VARCHAR(100),
    total_weight DECIMAL(10, 2),
    total_volume DECIMAL(10, 2),
    fragile BOOLEAN DEFAULT FALSE,
    delivery_cost DECIMAL(10, 2),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_deliveries_order_id
    ON delivery_schema.deliveries(order_id);

CREATE INDEX IF NOT EXISTS idx_deliveries_state
    ON delivery_schema.deliveries(delivery_state);