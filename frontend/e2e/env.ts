/*
 * Entorno aislado para los tests e2e: base, backend y frontend propios, en puertos distintos de los de
 * desarrollo (kiosco_db, 8080, 3001). Así el robot no ensucia tus datos ni choca con lo que tengas abierto.
 *
 *   Postgres  localhost:5433/kiosco_e2e   (mismo contenedor de docker compose, otra base)
 *   Backend   localhost:8081              (./mvnw spring-boot:run; Flyway crea las tablas)
 *   Frontend  localhost:3100              (next build + next start: lo más parecido a producción)
 */
export const BACKEND_PORT = 8081;
export const FRONTEND_PORT = 3100;
export const BACKEND_ORIGIN = `http://localhost:${BACKEND_PORT}`;
/** Directo al backend: solo para preparar datos. La pantalla pasa por el proxy /api del frontend. */
export const API_URL = `${BACKEND_ORIGIN}/api`;
export const FRONTEND_URL = `http://localhost:${FRONTEND_PORT}`;

// Credenciales de una base descartable: el backend crea este admin al arrancar si la base no tiene ninguno.
export const E2E_ADMIN = {
  username: process.env.E2E_ADMIN_USERNAME ?? 'e2e-admin',
  password: process.env.E2E_ADMIN_PASSWORD ?? 'e2e-admin-password',
};
