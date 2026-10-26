/** Contratos de ventas con la API (DTOs del backend: SaleResponseDTO, SaleDetailDTO, etc.). */

export type PaymentMethod = 'CASH' | 'MERCADO_PAGO';

export interface SaleDetail {
    productId: number;
    productName: string;
    quantity: number;
    /** Precio del momento de la venta: no cambia si después cambia el del producto. */
    unitPrice: number;
    subtotal: number;
}

export interface Sale {
    id: number;
    /** Instante ISO-8601 en UTC; se muestra en hora de Argentina con lib/time. */
    dateTime: string;
    totalAmount: number;
    username: string;
    details: SaleDetail[];
    /** null en ventas viejas sin medio registrado: cuentan como efectivo. */
    paymentMethod: PaymentMethod | null;
    /** Los tres son null mientras la venta está vigente. */
    voidedAt: string | null;
    voidedBy: string | null;
    voidReason: string | null;
}

/** Cierre de caja: totales sin las ventas anuladas. */
export interface DailySalesSummary {
    cashTotal: number;
    mpTotal: number;
    grandTotal: number;
}

export interface TopSeller {
    productId: number;
    name: string;
    quantity: number;
}
