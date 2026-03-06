CREATE SCHEMA IF NOT EXISTS shopping_cart_schema;
SET search_path TO shopping_cart_schema, public;

CREATE TABLE IF NOT EXISTS shopping_carts (
    shopping_cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(32) UNIQUE NOT NULL,
    cart_state VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_products (
    shopping_cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    price_at_add DECIMAL(19, 2),
    added_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (shopping_cart_id, product_id),
    FOREIGN KEY (shopping_cart_id) REFERENCES shopping_carts(shopping_cart_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_carts_username ON shopping_carts(username) WHERE cart_state = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_cart_products_cart ON cart_products(shopping_cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_products_product ON cart_products(product_id);

