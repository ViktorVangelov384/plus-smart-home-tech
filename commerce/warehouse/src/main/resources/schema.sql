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

CREATE TABLE IF NOT EXISTS warehouse_schema.bookings
(
    booking_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    delivery_id UUID
);

CREATE TABLE IF NOT EXISTS warehouse_schema.booking_products
(
    booking_id UUID NOT NULL REFERENCES warehouse_schema.bookings(booking_id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES warehouse_schema.warehouse_product(product_id) ON DELETE CASCADE,
    quantity BIGINT NOT NULL,
    CONSTRAINT pk_booking_products PRIMARY KEY (booking_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_booking_order_id
    ON warehouse_schema.bookings(order_id);

CREATE INDEX IF NOT EXISTS idx_warehouse_product_quantity
    ON warehouse_schema.warehouse_product(quantity);

CREATE INDEX IF NOT EXISTS idx_warehouse_product_fragile
    ON warehouse_schema.warehouse_product(fragile);