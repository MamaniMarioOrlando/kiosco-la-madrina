import { fileURLToPath } from 'node:url';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  resolve: {
    // El mismo alias que tsconfig.json: "@/lib/money" → src/lib/money.
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  test: {
    // Por ahora solo funciones puras: no hace falta simular un navegador (jsdom).
    environment: 'node',
    include: ['src/**/*.test.ts', '*.test.ts'],
  },
});
