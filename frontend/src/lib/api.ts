import axios from 'axios';
import { clearSession } from './session';

/**
 * Ruta del login. La usan authService para iniciar sesión y el interceptor de abajo para reconocer
 * esos pedidos: un solo lugar, así no pueden quedar desincronizadas si el backend la cambia.
 */
export const SIGNIN_PATH = '/auth/signin';

/**
 * Mismo dominio que la página: el proxy de Next (src/app/api/[...path]/route.ts) reenvía al backend.
 * El navegador ya no conoce la dirección de Railway.
 *
 * Sin "Authorization": la sesión es una cookie httpOnly que el navegador agrega solo. Como la agrega
 * también en pedidos armados por otras páginas, el backend exige el token CSRF en los que cambian datos:
 * axios lo lee de la cookie XSRF-TOKEN y lo manda en X-XSRF-TOKEN (solo a nuestro propio dominio).
 */
const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
    // Son los valores por defecto de axios; se escriben para que el contrato con Spring quede a la vista.
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN',
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Un 401 en el login significa credenciales inválidas, no sesión vencida:
        // redirigir recargaría la página y borraría el mensaje de error.
        const isLoginRequest = error.config?.url?.includes(SIGNIN_PATH);
        if (error.response?.status === 401 && !isLoginRequest) {
            if (typeof window !== 'undefined') {
                clearSession();
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
