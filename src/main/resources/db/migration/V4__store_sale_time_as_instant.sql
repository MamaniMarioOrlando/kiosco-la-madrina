-- La hora de venta pasa a ser un instante (TIMESTAMP WITH TIME ZONE) en lugar de una hora "de pared" sin zona.
-- Supuesto documentado: las ventas existentes se guardaron con el reloj del servidor de Railway, que corre en UTC.
ALTER TABLE sales ALTER COLUMN date_time TYPE TIMESTAMP WITH TIME ZONE USING date_time AT TIME ZONE 'UTC';
