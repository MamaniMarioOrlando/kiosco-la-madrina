-- Anular una venta no la borra: queda en el historial con cuándo, quién y por qué se anuló.
ALTER TABLE sales ADD COLUMN voided_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE sales ADD COLUMN voided_by_id BIGINT REFERENCES users (id);
ALTER TABLE sales ADD COLUMN void_reason VARCHAR(255);

-- Los tres datos van juntos: no puede haber una anulación sin responsable o sin motivo.
ALTER TABLE sales ADD CONSTRAINT chk_sale_void_complete CHECK (
    (voided_at IS NULL AND voided_by_id IS NULL AND void_reason IS NULL)
    OR (voided_at IS NOT NULL AND voided_by_id IS NOT NULL AND void_reason IS NOT NULL));
