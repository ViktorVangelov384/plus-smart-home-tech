CREATE SCHEMA IF NOT EXISTS shopping_store_schema;
SET search_path TO shopping_store_schema, public;

CREATE TABLE IF NOT EXISTS product (
    product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_src VARCHAR(512),
    product_category VARCHAR(20) NOT NULL,
    product_state VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    quantity_state VARCHAR(20) NOT NULL DEFAULT 'ENDED',
    price DECIMAL(19, 2) NOT NULL CHECK (price >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_category
    ON product(product_category) WHERE product_state != 'DELETED';

CREATE INDEX IF NOT EXISTS idx_product_state
    ON product(product_state);

CREATE INDEX IF NOT EXISTS idx_quantity_state ON shopping_store_schema.product(quantity_state);

CREATE INDEX IF NOT EXISTS idx_product_category_state ON shopping_store_schema.product(product_category, product_state);
