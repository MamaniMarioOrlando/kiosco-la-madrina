-- Incidente del 2026-09-20 (docs/incidents.md): la venta #12 se hizo con un precio de prueba de $35 billones
-- y distorsiona los totales históricos. No es una venta real, así que se borra.
-- El total funciona como candado: en cualquier otra base (local, tests) donde la venta 12 sea otra, no se borra nada.
-- El stock de Talco no se toca: fue editado a mano después y no se puede saber si ya contempla esta venta.

DELETE FROM sale_details
WHERE sale_id IN (SELECT id FROM sales WHERE id = 12 AND total_amount = 35006598300000.00);

DELETE FROM sales
WHERE id = 12 AND total_amount = 35006598300000.00;
