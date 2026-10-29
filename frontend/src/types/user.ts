/** Contratos de usuarios con la API (UserResponseDTO del backend). */

export type Role = 'ADMIN' | 'EMPLOYEE';

export interface User {
    id: number;
    username: string;
    /** Opcional: null si no se cargó. */
    email: string | null;
    role: Role;
    active: boolean;
}

/** Alta de un usuario por el admin (CreateUserRequestDTO). */
export interface CreateUserRequest {
    username: string;
    email: string;
    password: string;
    role: Role;
}
