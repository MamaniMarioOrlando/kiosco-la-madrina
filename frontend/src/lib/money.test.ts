import { describe, expect, it } from 'vitest';
import { fromCents, toCents } from '@/lib/money';

describe('toCents', () => {
  it('turns pesos with cents into an exact integer', () => {
    expect(toCents(1900.02)).toBe(190002);
    expect(toCents(0)).toBe(0);
  });

  it('absorbs the binary floating point error', () => {
    // 0.1 + 0.2 = 0.30000000000000004 en JavaScript.
    expect(toCents(0.1 + 0.2)).toBe(30);
  });

  it('round-trips every price with two decimals up to $20.000', () => {
    // Prueba exhaustiva: 2 millones de casos tardan milisegundos y cubren todos los precios reales.
    for (let cents = 0; cents <= 2_000_000; cents++) {
      if (toCents(cents / 100) !== cents) {
        throw new Error(`toCents(${cents / 100}) !== ${cents}`);
      }
    }
  });
});

describe('cart total in cents (the bug fixed in PR #29)', () => {
  const total = (prices: number[]) => fromCents(prices.reduce((sum, p) => sum + toCents(p), 0));

  it('adds 100.01 + 200.02 to exactly 300.03', () => {
    expect(100.01 + 200.02).not.toBe(300.03); // el error original, para que quede documentado
    expect(total([100.01, 200.02])).toBe(300.03);
  });

  it('gives zero change when the customer pays the exact total', () => {
    const prices = [7000.03, 980.04];
    const change = fromCents(toCents(7980.07) - prices.reduce((sum, p) => sum + toCents(p), 0));
    expect(change).toBe(0);
  });
});
