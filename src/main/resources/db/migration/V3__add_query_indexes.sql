-- PostgreSQL no indexa solo las claves foráneas. Un índice por cada consulta real que lo necesita:

-- Historial y resúmenes de ventas filtran por rango de fechas.
CREATE INDEX idx_sales_date_time ON sales (date_time);

-- Cargar los ítems de cada venta del historial.
CREATE INDEX idx_sale_details_sale_id ON sale_details (sale_id);

-- Contar los productos de una categoría antes de borrarla.
CREATE INDEX idx_products_category_id ON products (category_id);
