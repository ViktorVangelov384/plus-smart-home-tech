CREATE SCHEMA IF NOT EXISTS warehouse_schema;

CREATE TABLE IF NOT EXISTS warehouse_schema.warehouse_product (
    product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    width DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    depth DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    fragile BOOLEAN NOT NULL DEFAULT FALSE,
    quantity BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_warehouse_product_quantity
    ON warehouse_schema.warehouse_product(quantity);

CREATE INDEX IF NOT EXISTS idx_warehouse_product_fragile
    ON warehouse_schema.warehouse_product(fragile);