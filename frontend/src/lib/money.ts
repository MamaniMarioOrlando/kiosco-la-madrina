/**
 * Cuentas de dinero en centavos enteros. El number de JavaScript no representa exacto la mayoría
 * de los decimales (0.1 + 0.2 = 0.30000000000000004), así que sumar precios con centavos puede dar
 * un total apenas mayor al real. Con enteros no hay error: 10001 + 20002 = 30003 siempre.
 *
 * El backend sigue siendo la fuente de verdad del total (BigDecimal); esto evita que la pantalla de
 * cobro muestre un vuelto negativo cuando el cliente paga justo.
 */
export function toCents(amount: number): number {
    return Math.round(amount * 100);
}

export function fromCents(cents: number): number {
    return cents / 100;
}
