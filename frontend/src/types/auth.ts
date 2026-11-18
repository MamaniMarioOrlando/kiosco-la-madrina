/** Contratos de autenticación con la API (AuthRequestDTO y JwtResponseDTO del backend). */

export interface LoginRequest {
    username: string;
    password: string;
}

/**
 * El backend también devuelve el JWT en el cuerpo (para clientes con "Authorization: Bearer"), pero acá
 * no se declara a propósito: el navegador usa la cookie httpOnly y ningún código debería tocar el token.
 */
export interface LoginResponse {
    id: number;
    username: string;
    email: string | null;
    avatarUrl: string | null;
    /** Autoridades del usuario, p. ej. ["ADMIN"]. */
    roles: string[];
}
