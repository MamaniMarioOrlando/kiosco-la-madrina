import api from '@/lib/api';
import type { Product, ProductRequest } from '@/types/product';

/** Operaciones de productos contra la API (ver categoryService para el criterio de la capa). */
export const productService = {
    /** Productos a la venta (activos). */
    findAll: () => api.get<Product[]>('/products').then((res) => res.data),

    /** Productos desactivados, que el admin puede volver a poner a la venta. */
    findInactive: () => api.get<Product[]>('/products/inactive').then((res) => res.data),

    /** Productos para reponer, del más urgente al menos urgente. El umbral lo decide el backend. */
    findLowStock: () => api.get<Product[]>('/products/low-stock').then((res) => res.data),

    create: (product: ProductRequest) =>
        api.post<Product>('/products', product).then((res) => res.data),

    update: (id: number, product: ProductRequest) =>
        api.put<Product>(`/products/${id}`, product).then((res) => res.data),

    /** Borrado lógico: el producto se desactiva y deja de estar a la venta. */
    remove: (id: number) => api.delete<void>(`/products/${id}`).then(() => undefined),

    setActive: (id: number, active: boolean) =>
        api.patch<Product>(`/products/${id}/status`, { active }).then((res) => res.data),
};
