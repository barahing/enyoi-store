CREATE TABLE IF NOT EXISTS purchase_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_name VARCHAR(255),
    product_id UUID,
    quantity INT,
    unit_cost DECIMAL(10,2),
    status VARCHAR(20),
    order_date TIMESTAMP,
    delivery_date TIMESTAMP
);
