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
