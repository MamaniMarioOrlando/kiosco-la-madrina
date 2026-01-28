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
}

export default function SalesHistoryPage() {
    const [sales, setSales] = useState<Sale[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedSale, setSelectedSale] = useState<Sale | null>(null);

    useEffect(() => {
        fetchSales();
    }, []);

    const fetchSales = async () => {
        try {
            const response = await api.get('/sales');
            setSales(response.data.sort((a: Sale, b: Sale) =>
                new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime()
            ));
        } catch (err) {
            console.error('Error fetching sales', err);
        } finally {
            setLoading(false);
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
            <div>
                <h2 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
                    <History className="h-6 w-6 text-orange-600" /> Historial de Ventas
                </h2>
                <p className="text-slate-500">Revisa todas las transacciones realizadas.</p>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden">
                <Table>
                    <TableHeader>
                        <TableRow className="bg-slate-50">
                            <TableHead>Fecha y Hora</TableHead>
                            <TableHead>Vendedor</TableHead>
                            <TableHead>Total</TableHead>
                            <TableHead className="w-24">Acciones</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {sales.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={4} className="text-center py-10 text-slate-500">
                                    No hay ventas registradas todavía.
                                </TableCell>
                            </TableRow>
                        ) : (
                            sales.map((sale) => (
                                <TableRow key={sale.id}>
                                    <TableCell>
                                        <div className="flex items-center gap-2">
                                            <Calendar className="h-4 w-4 text-slate-400" />
                                            {new Date(sale.dateTime).toLocaleString()}
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <div className="flex items-center gap-2">
                                            <User className="h-4 w-4 text-slate-400" />
                                            <span className="font-medium">{sale.username}</span>
                                        </div>
                                    </TableCell>
                                    <TableCell className="font-bold text-slate-900">
                                        ${sale.totalAmount.toFixed(2)}
                                    </TableCell>
                                    <TableCell>
                                        <Button
                                            variant="ghost"
                                            size="sm"
                                            onClick={() => setSelectedSale(sale)}
                                            className="text-orange-600 hover:text-orange-700 hover:bg-orange-50"
                                        >
                                            <Eye className="h-4 w-4 mr-1" /> Detalles
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {/* Modal de Detalle */}
            <AnimatePresence>
                {selectedSale && (
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
                        <motion.div
                            initial={{ scale: 0.9, opacity: 0 }}
                            animate={{ scale: 1, opacity: 1 }}
                            exit={{ scale: 0.9, opacity: 0 }}
                            className="bg-white rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden"
                        >
                            <div className="p-6 border-b flex items-center justify-between bg-slate-900 text-white">
                                <div>
                                    <h3 className="text-xl font-bold">Venta #{selectedSale.id}</h3>
                                    <p className="text-sm text-slate-400">
                                        {new Date(selectedSale.dateTime).toLocaleString()}
                                    </p>
                                </div>
                                <Button variant="ghost" onClick={() => setSelectedSale(null)} className="text-white hover:bg-slate-800">
                                    <X className="h-6 w-6" />
                                </Button>
                            </div>

                            <div className="p-6">
                                <Table>
                                    <TableHeader>
                                        <TableRow>
                                            <TableHead>Producto</TableHead>
                                            <TableHead className="text-center">Cant.</TableHead>
                                            <TableHead className="text-right">Unitario</TableHead>
                                            <TableHead className="text-right">Subtotal</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {selectedSale.details.map((detail, idx) => (
                                            <TableRow key={idx}>
                                                <TableCell className="font-medium">{detail.productName}</TableCell>
                                                <TableCell className="text-center">{detail.quantity}</TableCell>
                                                <TableCell className="text-right">${detail.unitPrice.toFixed(2)}</TableCell>
                                                <TableCell className="text-right font-bold">${detail.subtotal.toFixed(2)}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>

                                <div className="mt-6 flex justify-between items-center bg-slate-50 p-4 rounded-xl border border-slate-100">
                                    <span className="text-slate-600 font-medium">Vendedor: {selectedSale.username}</span>
                                    <div className="text-right">
                                        <div className="text-sm text-slate-500">Total Venta</div>
                                        <div className="text-3xl font-black text-slate-900">${selectedSale.totalAmount.toFixed(2)}</div>
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
