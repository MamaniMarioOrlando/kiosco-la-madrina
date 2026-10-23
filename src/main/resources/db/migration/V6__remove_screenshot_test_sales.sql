-- Las ventas #16 y #17 (2026-10-23) se hicieron en producción para sacar capturas: no son ventas reales.
-- Se borran y se devuelve el stock que restaron. Acá sí es seguro: nadie editó esos productos después,
-- así que sumar lo vendido es la operación inversa exacta.
-- Candado: id + total + instante exacto. En cualquier otra base no coincide nada y no se toca nada.

CREATE TEMPORARY TABLE test_sales ON COMMIT DROP AS
SELECT id FROM sales
WHERE (id = 16 AND total_amount = 11600.00 AND date_time = '2026-10-23 12:41:13.087377+00')
   OR (id = 17 AND total_amount = 6980.00 AND date_time = '2026-10-23 12:43:21.212679+00');

UPDATE products p
SET stock_quantity = p.stock_quantity + sold.quantity
FROM (SELECT product_id, SUM(quantity) AS quantity
      FROM sale_details
      WHERE sale_id IN (SELECT id FROM test_sales)
      GROUP BY product_id) AS sold
WHERE p.id = sold.product_id;

DELETE FROM sale_details WHERE sale_id IN (SELECT id FROM test_sales);
DELETE FROM sales WHERE id IN (SELECT id FROM test_sales);
