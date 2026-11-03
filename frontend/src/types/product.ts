/** Lo decide el backend (StockLevel): CRITICAL con menos de 3 unidades, LOW con menos de 5. */
export type StockLevel = 'CRITICAL' | 'LOW' | 'OK';

/**
 * Producto tal como lo devuelve la API (ProductDTO del backend). Es el contrato con el servidor:
 * se declara una sola vez y todas las pantallas lo importan, así no hay copias que se desincronicen.
 */
export interface Product {
    id: number;
    barcode: string;
    name: string;
    price: number;
    stockQuantity: number;
    /** null si el producto no tiene categoría. */
    categoryId: number | null;
    categoryName: string | null;
    active: boolean;
    /** Calculado por el backend: el frontend no repite los umbrales, solo decide cómo pintar cada nivel. */
    stockLevel: StockLevel;
}

/** Lo que se envía al crear o editar: id, active y stockLevel los maneja el backend, categoryName lo deriva de categoryId. */
export type ProductRequest = Omit<Product, 'id' | 'active' | 'categoryName' | 'stockLevel'>;
