/** Contratos de autenticación con la API (AuthRequestDTO y JwtResponseDTO del backend). */

export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    token: string;
    /** Siempre "Bearer". */
    type: string;
    id: number;
    username: string;
    email: string | null;
    avatarUrl: string | null;
    /** Autoridades del usuario, p. ej. ["ADMIN"]. */
    roles: string[];
}
