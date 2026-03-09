CREATE SCHEMA IF NOT EXISTS order_schema;

CREATE TABLE IF NOT EXISTS order_schema.orders
(
    order_id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    shopping_cart_id UUID,
    payment_id UUID,
    delivery_id UUID,
    state VARCHAR(50) NOT NULL,
    delivery_weight DECIMAL(10, 2),
    delivery_volume DECIMAL(10, 2),
    fragile BOOLEAN,
    total_price DECIMAL(10, 2),
    delivery_price DECIMAL(10, 2),
    product_price DECIMAL(10, 2),
    delivery_country VARCHAR(100),
    delivery_city VARCHAR(100),
    delivery_street VARCHAR(100),
    delivery_house VARCHAR(100),
    delivery_flat VARCHAR(100),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_schema.order_products
(
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES order_schema.orders(order_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_orders_username
    ON order_schema.orders(username);

CREATE INDEX IF NOT EXISTS idx_orders_state
    ON order_schema.orders(state);

CREATE INDEX IF NOT EXISTS idx_orders_payment_id
    ON order_schema.orders(payment_id);

CREATE INDEX IF NOT EXISTS idx_orders_delivery_id
    ON order_schema.orders(delivery_id);

CREATE INDEX IF NOT EXISTS idx_orders_created_at
    ON order_schema.orders(created_at);