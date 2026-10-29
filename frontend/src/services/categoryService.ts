import api from '@/lib/api';
import type { Category, CategoryRequest } from '@/types/category';

/**
 * Operaciones de categorías contra la API. Las pantallas piden datos de negocio y no conocen
 * URLs ni axios: si cambia un endpoint o el cliente HTTP, se toca solo este archivo.
 */
export const categoryService = {
    findAll: () => api.get<Category[]>('/categories').then((res) => res.data),

    create: (category: CategoryRequest) =>
        api.post<Category>('/categories', category).then((res) => res.data),

    update: (id: number, category: CategoryRequest) =>
        api.put<Category>(`/categories/${id}`, category).then((res) => res.data),

    remove: (id: number) => api.delete<void>(`/categories/${id}`).then(() => undefined),
};
