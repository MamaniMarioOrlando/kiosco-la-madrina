-- La base es la última línea de defensa: rechaza datos imposibles aunque no pasen por la aplicación.
-- Una sentencia por cambio: SQL portable, también lo entiende el H2 de ProductRepositoryTest.

ALTER TABLE products ADD CONSTRAINT chk_product_stock_not_negative CHECK (stock_quantity >= 0);
ALTER TABLE products ADD CONSTRAINT chk_product_price_positive CHECK (price > 0);

ALTER TABLE sale_details ADD CONSTRAINT chk_sdetail_quantity_positive CHECK (quantity > 0);
ALTER TABLE sale_details ALTER COLUMN sale_id SET NOT NULL;
ALTER TABLE sale_details ALTER COLUMN product_id SET NOT NULL;

-- Los usuarios nunca se borran (se desactivan), así que toda venta conserva a quien la hizo.
ALTER TABLE sales ALTER COLUMN user_id SET NOT NULL;
