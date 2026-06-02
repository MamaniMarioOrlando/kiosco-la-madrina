'use client';

import { useEffect, useState } from 'react';
import api from '@/lib/api';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Loader2, History, User, Calendar, Eye, X } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { formatCurrency } from '@/lib/utils';

interface SaleDetail {
    productName: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
}

interface Sale {
    id: number;
    dateTime: string;
    totalAmount: number;
    username: string;
    details: SaleDetail[];
    paymentMethod?: 'CASH' | 'MERCADO_PAGO';
}

export default function SalesHistoryPage() {
    const [sales, setSales] = useState<Sale[]>([]);
    const [summary, setSummary] = useState({ cashTotal: 0, mpTotal: 0, grandTotal: 0 });
    const [loading, setLoading] = useState(true);
    const [pageLoading, setPageLoading] = useState(false);
    const [selectedSale, setSelectedSale] = useState<Sale | null>(null);
    const [viewMode, setViewMode] = useState<'TODAY' | 'ALL'>('TODAY');
    const [currentPage, setCurrentPage] = useState(0); // 0-indexed for backend
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const ITEMS_PER_PAGE = 8;

    useEffect(() => {
        fetchData(0, viewMode);
    }, [viewMode]);

    const fetchData = async (page: number, mode: 'TODAY' | 'ALL') => {
        setPageLoading(true);
        try {
            const dateStr = new Date().toISOString().split('T')[0];
            const dateParam = mode === 'TODAY' ? `date=${dateStr}&` : '';
            
            const [salesRes, summaryRes] = await Promise.all([
                api.get(`/sales?${dateParam}page=${page}&size=${ITEMS_PER_PAGE}`),
                api.get(`/sales/summary?${dateParam.replace('&', '')}`)
            ]);
            
            setSales(salesRes.data.content);
            setTotalPages(salesRes.data.totalPages);
            setTotalElements(salesRes.data.totalElements);
            setCurrentPage(page);

            if (summaryRes.data) {
                setSummary(summaryRes.data);
            }
        } catch (err) {
            console.error('Error fetching sales', err);
        } finally {
            setLoading(false);
            setPageLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex h-full items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
                        <History className="h-6 w-6 text-orange-600" /> Historial de Ventas
                    </h2>
                    <p className="text-slate-500">Cuadre de caja y revisión de transacciones.</p>
                </div>
                <div className="flex gap-2">
                    <Button 
                        variant={viewMode === 'TODAY' ? 'default' : 'outline'} 
                        className={viewMode === 'TODAY' ? 'bg-orange-600 hover:bg-orange-700' : ''}
                        onClick={() => { setViewMode('TODAY'); fetchData(0, 'TODAY'); }}
                    >
                        Cierre de Hoy
                    </Button>
                    <Button 
                        variant={viewMode === 'ALL' ? 'default' : 'outline'} 
                        className={viewMode === 'ALL' ? 'bg-slate-800 hover:bg-slate-900 text-white' : ''}
                        onClick={() => { setViewMode('ALL'); fetchData(0, 'ALL'); }}
                    >
                        Histórico Completo
                    </Button>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-4 rounded-xl shadow-sm flex flex-col items-center justify-center text-center">
                    <div className="text-xs uppercase font-bold text-slate-500 mb-1">
                        {viewMode === 'TODAY' ? 'Total Ventas Hoy' : 'Total Histórico'}
                    </div>
                    <div className="text-3xl font-black text-slate-900 dark:text-white">{formatCurrency(summary.grandTotal)}</div>
                </div>
                <div className="bg-orange-50 border border-orange-100 p-4 rounded-xl shadow-sm flex flex-col items-center justify-center text-center dark:bg-orange-950/20 dark:border-orange-900/50">
                    <div className="text-xs uppercase font-bold text-orange-600 mb-1">💵 Ingreso Efectivo</div>
                    <div className="text-3xl font-black text-orange-600 dark:text-orange-400">{formatCurrency(summary.cashTotal)}</div>
                </div>
                <div className="bg-[#009EE3]/5 border border-[#009EE3]/20 p-4 rounded-xl shadow-sm flex flex-col items-center justify-center text-center">
                    <div className="text-xs uppercase font-bold text-[#009EE3] mb-1">💳 Ingreso Mercado Pago</div>
                    <div className="text-3xl font-black text-[#009EE3]">{formatCurrency(summary.mpTotal)}</div>
                </div>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-100 dark:border-slate-800 overflow-hidden">
                <Table>
                    <TableHeader>
                        <TableRow className="bg-slate-50 dark:bg-slate-800/50 border-slate-100 dark:border-slate-800">
                            <TableHead className="dark:text-slate-400">Fecha y Hora</TableHead>
                            <TableHead className="dark:text-slate-400">Vendedor</TableHead>
                            <TableHead className="dark:text-slate-400">Método de Pago</TableHead>
                            <TableHead className="dark:text-slate-400">Total</TableHead>
                            <TableHead className="w-24 dark:text-slate-400">Acciones</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {sales.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} className="text-center py-10 text-slate-500">
                                    No hay ventas registradas.
                                </TableCell>
                            </TableRow>
                        ) : (
                            sales.map((sale) => (
                                <TableRow key={sale.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors cursor-default">
                                    <TableCell>
                                        <div className="flex items-center gap-2 dark:text-slate-300">
                                            <Calendar className="h-4 w-4 text-slate-400 dark:text-slate-500" />
                                            {new Date(sale.dateTime).toLocaleString('es-AR')}
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <div className="flex items-center gap-2 dark:text-slate-300">
                                            <User className="h-4 w-4 text-slate-400 dark:text-slate-500" />
                                            <span className="font-medium">{sale.username}</span>
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        {sale.paymentMethod === 'MERCADO_PAGO' ? (
                                            <span className="inline-flex items-center gap-1 px-2 py-1 rounded bg-[#009EE3]/10 text-[#009EE3] text-xs font-bold font-mono border border-[#009EE3]/20">
                                                💳 M.PAGO
                                            </span>
                                        ) : (
                                            <span className="inline-flex items-center gap-1 px-2 py-1 rounded bg-orange-100 text-orange-700 text-xs font-bold font-mono border border-orange-200 dark:bg-orange-950/30 dark:border-orange-900/50 dark:text-orange-400">
                                                💵 EFECTIVO
                                            </span>
                                        )}
                                    </TableCell>
                                    <TableCell className="font-bold text-slate-900 dark:text-white text-lg">
                                        {formatCurrency(sale.totalAmount)}
                                    </TableCell>
                                    <TableCell>
                                        <Button
                                            variant="ghost"
                                            size="sm"
                                            onClick={() => setSelectedSale(sale)}
                                            className="text-orange-600 hover:text-orange-700 hover:bg-orange-50 dark:hover:bg-orange-950/20"
                                        >
                                            <Eye className="h-4 w-4 mr-1" /> Detalles
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
                
                {totalPages > 0 && (
                    <div className="flex justify-between items-center p-4 border-t border-slate-100 dark:border-slate-800 bg-slate-50 dark:bg-slate-900">
                        <span className="text-sm text-slate-500 font-medium">
                            Página {currentPage + 1} de {totalPages} ({totalElements} registros)
                        </span>
                        <div className="flex gap-2">
                            <Button 
                                variant="outline" size="sm" 
                                disabled={currentPage === 0 || pageLoading} 
                                onClick={() => fetchData(currentPage - 1, viewMode)}>
                                Anterior
                            </Button>
                            <Button 
                                variant="outline" size="sm" 
                                disabled={currentPage >= totalPages - 1 || pageLoading} 
                                onClick={() => fetchData(currentPage + 1, viewMode)}>
                                Siguiente
                            </Button>
                        </div>
                    </div>
                )}
            </div>

            {/* Modal de Detalle */}
            <AnimatePresence>
                {selectedSale && (
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
                        <motion.div
                            initial={{ scale: 0.9, opacity: 0 }}
                            animate={{ scale: 1, opacity: 1 }}
                            exit={{ scale: 0.9, opacity: 0 }}
                            className="bg-white dark:bg-slate-900 rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden"
                        >
                            <div className="p-6 border-b dark:border-slate-800 flex items-center justify-between bg-slate-900 text-white">
                                <div>
                                    <h3 className="text-xl font-bold">Venta #{selectedSale.id}</h3>
                                    <p className="text-sm text-slate-400">
                                        {new Date(selectedSale.dateTime).toLocaleString('es-AR')}
                                    </p>
                                </div>
                                <Button
                                    variant="ghost"
                                    onClick={() => setSelectedSale(null)}
                                    className="text-white hover:text-orange-500 hover:bg-orange-500/10 transition-colors"
                                >
                                    <X className="h-6 w-6" />
                                </Button>
                            </div>

                            <div className="p-6">
                                <Table>
                                    <TableHeader>
                                        <TableRow className="dark:border-slate-800">
                                            <TableHead>Producto</TableHead>
                                            <TableHead className="text-center">Cant.</TableHead>
                                            <TableHead className="text-right">Unitario</TableHead>
                                            <TableHead className="text-right">Subtotal</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {selectedSale.details.map((detail, idx) => (
                                            <TableRow key={idx} className="dark:border-slate-800">
                                                <TableCell className="font-medium dark:text-slate-300">{detail.productName}</TableCell>
                                                <TableCell className="text-center dark:text-slate-300">{detail.quantity}</TableCell>
                                                <TableCell className="text-right dark:text-slate-300">{formatCurrency(detail.unitPrice)}</TableCell>
                                                <TableCell className="text-right font-bold dark:text-white">{formatCurrency(detail.subtotal)}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>

                                <div className="mt-6 flex justify-between items-center bg-slate-50 dark:bg-slate-950 p-4 rounded-xl border border-slate-100 dark:border-slate-800">
                                    <span className="text-slate-600 dark:text-slate-400 font-medium">Vendedor: {selectedSale.username}</span>
                                    <div className="text-right">
                                        <div className="text-sm text-slate-500 dark:text-slate-500">Total Venta</div>
                                        <div className="text-3xl font-black text-slate-900 dark:text-white">{formatCurrency(selectedSale.totalAmount)}</div>
                                    </div>
                                </div>
                            </div>

                        </motion.div>
                    </div>
                )}
            </AnimatePresence>
        </div>
    );
}
