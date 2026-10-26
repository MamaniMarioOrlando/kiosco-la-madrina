/** Página de resultados de la API (PageResponseDTO del backend). Genérica: sirve para cualquier listado paginado. */
export interface PageResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    isLast: boolean;
}
