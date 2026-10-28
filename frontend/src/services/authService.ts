import api, { SIGNIN_PATH } from '@/lib/api';
import type { LoginRequest, LoginResponse } from '@/types/auth';

/**
 * Autenticación, separada de userService porque responde a otro actor (alguien que todavía no
 * entró) y cambia por otros motivos (p. ej. pasar el token a una cookie). Solo habla HTTP:
 * guardar la sesión en el navegador es responsabilidad de lib/session.
 */
export const authService = {
    login: (credentials: LoginRequest) =>
        api.post<LoginResponse>(SIGNIN_PATH, credentials).then((res) => res.data),
};
