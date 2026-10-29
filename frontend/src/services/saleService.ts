import api from '@/lib/api';
import type { PageResponse } from '@/types/page';
import type { DailySalesSummary, Sale, SalePageQuery, SaleRequest, TopSeller } from '@/types/sale';

/** Operaciones de ventas contra la API (ver categoryService para el criterio de la capa). */
export const saleService = {
    /** Historial paginado, de la más nueva a la más vieja. */
    findPage: (query: SalePageQuery) =>
        api.get<PageResponse<Sale>>('/sales', { params: query }).then((res) => res.data),

    /** Cierre de caja de un día (YYYY-MM-DD) o de todo el historial si no se indica. Excluye anuladas. */
    getSummary: (date?: string) =>
        api.get<DailySalesSummary>('/sales/summary', { params: { date } }).then((res) => res.data),

    findTopSellers: () => api.get<TopSeller[]>('/sales/top-sellers').then((res) => res.data),

    create: (sale: SaleRequest) => api.post<Sale>('/sales', sale).then((res) => res.data),

    /** Solo admin y solo ventas del día; el stock vuelve al inventario. */
    voidSale: (id: number, reason: string) =>
        api.post<Sale>(`/sales/${id}/void`, { reason }).then((res) => res.data),
};
