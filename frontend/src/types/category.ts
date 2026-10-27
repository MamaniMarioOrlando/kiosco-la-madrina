/** Categoría tal como la devuelve la API (CategoryDTO del backend). */
export interface Category {
    id: number;
    name: string;
    /** Opcional: null si no se cargó. */
    description: string | null;
}
