import { randomBytes } from 'node:crypto';
import { defineConfig, devices } from '@playwright/test';
import { API_URL, BACKEND_PORT, E2E_ADMIN, FRONTEND_PORT, FRONTEND_URL } from './e2e/env';

export default defineConfig({
  testDir: './e2e',
  // Un solo recorrido a la vez: los tests comparten base y stock, en paralelo se pisarían.
  workers: 1,
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : 'list',

  use: {
    baseURL: FRONTEND_URL,
    // Si un test falla, queda una grabación paso a paso para ver qué vio el robot (npx playwright show-trace).
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },

  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],

  webServer: [
    {
      command: './mvnw -q spring-boot:run',
      cwd: '..',
      // Sin token responde 401: alcanza para saber que el backend ya atiende pedidos.
      url: `${API_URL}/categories`,
      timeout: 180_000,
      reuseExistingServer: !process.env.CI,
      env: {
        PORT: String(BACKEND_PORT),
        DB_URL: process.env.E2E_DB_URL ?? 'jdbc:postgresql://localhost:5433/kiosco_e2e',
        DB_USERNAME: process.env.E2E_DB_USERNAME ?? 'postgres',
        DB_PASSWORD: process.env.E2E_DB_PASSWORD ?? 'password',
        ALLOWED_ORIGINS: FRONTEND_URL,
        // Una clave nueva en cada corrida: no hay ningún secreto guardado en el repositorio.
        JWT_SECRET_KEY: randomBytes(32).toString('base64'),
        ADMIN_USERNAME: E2E_ADMIN.username,
        ADMIN_PASSWORD: E2E_ADMIN.password,
      },
    },
    {
      command: `npm run build && npm run start -- -p ${FRONTEND_PORT}`,
      url: FRONTEND_URL,
      timeout: 180_000,
      reuseExistingServer: !process.env.CI,
      env: { NEXT_PUBLIC_API_URL: API_URL },
    },
  ],
});
