CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_key VARCHAR(255) NOT NULL,
    external_order_id VARCHAR(255),
    idempotency_key VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_error_code VARCHAR(50),
    last_error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_partner_idempotency UNIQUE (partner_key, idempotency_key)
);

CREATE INDEX idx_orders_partner_key ON orders(partner_key);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_partner_idempotency ON orders(partner_key, idempotency_key);
