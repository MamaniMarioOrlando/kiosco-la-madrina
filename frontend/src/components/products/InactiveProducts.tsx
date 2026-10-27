'use client';

import { useCallback, useEffect, useState } from 'react';
import { productService } from '@/services/productService';
import { apiErrorMessage } from '@/lib/errors';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Archive, Barcode, ChevronDown, RotateCcw } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { Product } from '@/types/product';


interface InactiveProductsProps {
    /** Cambia cada vez que se desactiva un producto, para volver a cargar la lista. */
    reloadSignal: number;
    /** Se llama después de reactivar, para que el catálogo lo vuelva a mostrar. */
    onReactivated: () => void;
}

/**
 * Productos desactivados (solo admin): no se venden ni aparecen en el catálogo, pero se pueden reactivar.
 * No se muestra nada si no hay inactivos.
 */
export function InactiveProducts({ reloadSignal, onReactivated }: InactiveProductsProps) {
    const [products, setProducts] = useState<Product[]>([]);
    const [expanded, setExpanded] = useState(false);
    const [reactivatingId, setReactivatingId] = useState<number | null>(null);

    const load = useCallback(async () => {
        try {
            setProducts(await productService.findInactive());
        } catch (err) {
            toast.error(apiErrorMessage(err, 'Error al cargar los productos inactivos'));
        }
    }, []);

    useEffect(() => {
        load();
    }, [load, reloadSignal]);

    const reactivate = async (product: Product) => {
        setReactivatingId(product.id);
        try {
            await productService.setActive(product.id, true);
            toast.success(`"${product.name}" vuelve a estar a la venta`);
            await load();
            onReactivated();
        } catch (err) {
            toast.error(apiErrorMessage(err, 'No se pudo reactivar el producto'));
        } finally {
            setReactivatingId(null);
        }
    };

    if (products.length === 0) return null;

    return (
        <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
            <button
                type="button"
                onClick={() => setExpanded(!expanded)}
                className="flex w-full items-center justify-between px-6 py-4 text-left hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
            >
                <span className="flex items-center gap-2 font-semibold text-slate-700 dark:text-slate-300">
                    <Archive className="h-4 w-4" />
                    Productos inactivos ({products.length})
                </span>
                <ChevronDown className={cn('h-4 w-4 text-slate-400 transition-transform', expanded && 'rotate-180')} />
            </button>

            {expanded && (
                <Table>
                    <TableHeader>
                        <TableRow className="bg-slate-50 dark:bg-slate-800/50 border-slate-100 dark:border-slate-800">
                            <TableHead className="dark:text-slate-400">Producto</TableHead>
                            <TableHead className="dark:text-slate-400">Categoría</TableHead>
                            <TableHead className="dark:text-slate-400">Stock</TableHead>
                            <TableHead className="w-32 dark:text-slate-400">Acciones</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {products.map((p) => (
                            <TableRow key={p.id} className="dark:border-slate-800 opacity-75">
                                <TableCell>
                                    <div className="font-medium dark:text-white">{p.name}</div>
                                    <div className="text-xs text-slate-500 dark:text-slate-400 font-mono flex items-center gap-1">
                                        <Barcode className="h-3 w-3" /> {p.barcode}
                                    </div>
                                </TableCell>
                                <TableCell className="text-slate-600 dark:text-slate-400">{p.categoryName ?? '-'}</TableCell>
                                <TableCell className="text-slate-600 dark:text-slate-400">{p.stockQuantity} unid.</TableCell>
                                <TableCell>
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        disabled={reactivatingId === p.id}
                                        onClick={() => reactivate(p)}
                                        className="text-emerald-600 dark:text-emerald-400"
                                    >
                                        <RotateCcw className="mr-1 h-3 w-3" />
                                        {reactivatingId === p.id ? 'Reactivando...' : 'Reactivar'}
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            )}
        </div>
    );
}
