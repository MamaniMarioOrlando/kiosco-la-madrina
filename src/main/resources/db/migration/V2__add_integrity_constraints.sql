-- La base es la última línea de defensa: rechaza datos imposibles aunque no pasen por la aplicación.
-- Una sentencia por cambio: SQL portable, también lo entiende el H2 de ProductRepositoryTest.

-- Mismos límites que ProductDTO; el máximo de precio cierra el incidente del 2026-09-20 (docs/incidents.md).
ALTER TABLE products ADD CONSTRAINT chk_product_stock_range CHECK (stock_quantity BETWEEN 0 AND 100000);
ALTER TABLE products ADD CONSTRAINT chk_product_price_range CHECK (price > 0 AND price <= 10000000);

ALTER TABLE sale_details ADD CONSTRAINT chk_sdetail_quantity_positive CHECK (quantity > 0);
ALTER TABLE sale_details ALTER COLUMN sale_id SET NOT NULL;
ALTER TABLE sale_details ALTER COLUMN product_id SET NOT NULL;

-- Los usuarios nunca se borran (se desactivan), así que toda venta conserva a quien la hizo.
ALTER TABLE sales ALTER COLUMN user_id SET NOT NULL;
