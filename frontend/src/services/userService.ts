import api from '@/lib/api';
import type { CreateUserRequest, User } from '@/types/user';

/** Administración de usuarios contra la API (ver categoryService para el criterio de la capa). */
export const userService = {
    /** Solo admin. */
    findAll: () => api.get<User[]>('/users').then((res) => res.data),

    /** Solo admin. */
    create: (user: CreateUserRequest) => api.post<User>('/users', user).then((res) => res.data),

    /** Solo admin. Un usuario desactivado pierde el acceso en el acto. */
    setActive: (id: number, active: boolean) =>
        api.patch<User>(`/users/${id}/status`, { active }).then((res) => res.data),

    /** Avatar del usuario logueado: el backend lo identifica por el token, no por un id. */
    updateMyAvatar: (avatarUrl: string) =>
        api.patch<void>('/users/me/avatar', { avatarUrl }).then(() => undefined),
};
