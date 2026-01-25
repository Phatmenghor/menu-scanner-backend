-- V6__create_delivery_options_table.sql
-- Migration script to create/update delivery_options table

-- Create the delivery_options table if it doesn't exist
CREATE TABLE IF NOT EXISTS delivery_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    business_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_delivery_options_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_delivery_options_business_id ON delivery_options(business_id);
CREATE INDEX IF NOT EXISTS idx_delivery_options_status ON delivery_options(status);
CREATE INDEX IF NOT EXISTS idx_delivery_options_is_deleted ON delivery_options(is_deleted);
CREATE INDEX IF NOT EXISTS idx_delivery_options_name ON delivery_options(name);

-- Add comments for documentation
COMMENT ON TABLE delivery_options IS 'Stores delivery options for businesses (e.g., Standard Delivery, Express Delivery)';
COMMENT ON COLUMN delivery_options.business_id IS 'Reference to the business this delivery option belongs to';
COMMENT ON COLUMN delivery_options.name IS 'Name of the delivery option (e.g., Standard Delivery, Express Delivery)';
COMMENT ON COLUMN delivery_options.price IS 'Price of the delivery option';
COMMENT ON COLUMN delivery_options.status IS 'Status of the delivery option (ACTIVE, INACTIVE)';
