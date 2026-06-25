CREATE TABLE IF NOT EXISTS products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description TEXT,
    price       NUMERIC(19, 2) NOT NULL,
    stock       INTEGER        NOT NULL DEFAULT 0,
    category    VARCHAR(100)   NOT NULL,
    sku         VARCHAR(100)   NOT NULL UNIQUE,
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_sku      ON products (sku);
CREATE INDEX idx_products_active   ON products (active);
CREATE INDEX idx_products_name     ON products (name);

COMMENT ON TABLE  products             IS 'Product catalog';
COMMENT ON COLUMN products.sku        IS 'Stock Keeping Unit — unique product identifier';
COMMENT ON COLUMN products.price      IS 'Unit price in base currency';
COMMENT ON COLUMN products.stock      IS 'Current units available';
