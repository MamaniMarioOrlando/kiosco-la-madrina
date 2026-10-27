'use client';

import { useEffect, useState } from 'react';
import { apiErrorMessage } from '@/lib/errors';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Plus, Loader2, Barcode, Archive, Pencil } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { InactiveProducts } from '@/components/products/InactiveProducts';
import { cn, formatCurrency } from '@/lib/utils';
import type { Product, ProductRequest } from '@/types/product';
import type { Category } from '@/types/category';
import { categoryService } from '@/services/categoryService';
import { productService } from '@/services/productService';



export default function ProductsPage() {
    const [products, setProducts] = useState<Product[]>([]);
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [isAdmin, setIsAdmin] = useState(false);

    // Form state
    const [isAdding, setIsAdding] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [formData, setFormData] = useState({
        barcode: '',
        name: '',
        price: '',
        stockQuantity: '',
        categoryId: ''
    });
    const [errors, setErrors] = useState<{ [key: string]: string }>({});
    const [submitting, setSubmitting] = useState(false);
    // Se incrementa al desactivar un producto para que la sección de inactivos se recargue.
    const [inactiveReloadSignal, setInactiveReloadSignal] = useState(0);

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            const user = JSON.parse(userData);
            setIsAdmin(user.roles?.some((r: string) => r.toLowerCase().includes('admin')));
        }
        Promise.all([fetchProducts(), fetchCategories()]).finally(() => setLoading(false));
    }, []);

    const fetchProducts = async () => {
        try {
            setProducts(await productService.findAll());
        } catch (err) {
            toast.error(apiErrorMessage(err, 'Error al cargar los productos'));
        }
    };

    const fetchCategories = async () => {
        try {
            setCategories(await categoryService.findAll());
        } catch (err) {
            toast.error(apiErrorMessage(err, 'Error al cargar las categorías'));
        }
    };

    const validateForm = () => {
        const newErrors: { [key: string]: string } = {};
        if (!formData.barcode.trim()) newErrors.barcode = 'Este campo es obligatorio';
        if (!formData.name.trim()) newErrors.name = 'Este campo es obligatorio';
        if (!formData.price || isNaN(parseFloat(formData.price)) || parseFloat(formData.price) <= 0) newErrors.price = 'El precio debe ser mayor a 0';
        if (parseFloat(formData.price) > 10000000) newErrors.price = 'El precio no puede superar los $10.000.000';
        if (!formData.stockQuantity || isNaN(parseInt(formData.stockQuantity)) || parseInt(formData.stockQuantity) < 0) newErrors.stockQuantity = 'El stock no puede ser negativo';
        if (parseInt(formData.stockQuantity) > 100000) newErrors.stockQuantity = 'El stock no puede superar las 100.000 unidades';
        if (!formData.categoryId) newErrors.categoryId = 'Este campo es obligatorio';
        
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;
        setSubmitting(true);
        try {
            const payload: ProductRequest = {
                barcode: formData.barcode,
                name: formData.name,
                price: parseFloat(formData.price),
                stockQuantity: parseInt(formData.stockQuantity),
                categoryId: formData.categoryId ? parseInt(formData.categoryId) : null
            };

            if (editingId) {
                await productService.update(editingId, payload);
            } else {
                await productService.create(payload);
            }

            setIsAdding(false);
            setEditingId(null);
            setFormData({ barcode: '', name: '', price: '', stockQuantity: '', categoryId: '' });
            setErrors({});
            fetchProducts();
        } catch (err) {
            // Incluye los 409 del backend, p. ej. código de barras repetido o de un producto inactivo.
            toast.error(apiErrorMessage(err, `Error al ${editingId ? 'actualizar' : 'crear'} el producto`));
        } finally {
            setSubmitting(false);
        }
    };

    const handleEdit = (p: Product) => {
        setEditingId(p.id);
        setFormData({
            barcode: p.barcode,
            name: p.name,
            price: p.price.toString(),
            stockQuantity: p.stockQuantity.toString(),
            categoryId: p.categoryId?.toString() ?? ''
        });
        setErrors({});
        setIsAdding(true);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    // Borrado lógico: el producto deja de venderse pero queda en el historial y se puede reactivar.
    const handleDeactivate = async (product: Product) => {
        if (!confirm(`¿Desactivar "${product.name}"? Va a dejar de aparecer en ventas. Podés reactivarlo más tarde.`)) return;
        try {
            await productService.remove(product.id);
            toast.success(`"${product.name}" desactivado`);
            fetchProducts();
            setInactiveReloadSignal((n) => n + 1);
        } catch (err) {
            toast.error(apiErrorMessage(err, 'No se pudo desactivar el producto'));
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
            <div className="flex items-center justify-between">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Productos</h2>
                    <p className="text-slate-500 dark:text-slate-400">Administra el inventario y precios.</p>
                </div>
                {isAdmin && (
                    <Button
                        onClick={() => {
                            if (isAdding) {
                                setIsAdding(false);
                                setEditingId(null);
                                setFormData({ barcode: '', name: '', price: '', stockQuantity: '', categoryId: '' });
                                setErrors({});
                            } else {
                                setIsAdding(true);
                            }
                        }}
                        className="bg-orange-600 hover:bg-orange-700"
                    >
                        {isAdding && editingId ? 'Cancelar Edición' : (isAdding ? 'Cancelar' : <><Plus className="mr-2 h-4 w-4" /> Nuevo Producto</>)}
                    </Button>
                )}
            </div>

            <AnimatePresence>
                {isAdmin && isAdding && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: 'auto', opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        className="overflow-hidden"
                    >
                        <div className="bg-white dark:bg-slate-900 p-6 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 mb-6">
                            <h3 className="text-lg font-semibold mb-4 text-slate-800 dark:text-slate-200">
                                {editingId ? 'Editar Producto' : 'Nuevo Producto'}
                            </h3>
                            <form onSubmit={handleSubmit} noValidate className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 items-start">
                                <div className="space-y-2">
                                    <Label>Código de Barras <span className="text-red-500">*</span></Label>
                                    <Input
                                        placeholder="Ej: 7791234567890"
                                        value={formData.barcode}
                                        onChange={(e) => {
                                            setFormData({ ...formData, barcode: e.target.value });
                                            if (errors.barcode) setErrors({ ...errors, barcode: '' });
                                        }}
                                        className={errors.barcode ? "border-red-500 focus-visible:ring-red-500" : ""}
                                    />
                                    {errors.barcode && <p className="text-xs text-red-500 dark:text-red-400">{errors.barcode}</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label>Nombre <span className="text-red-500">*</span></Label>
                                    <Input
                                        placeholder="Ej: Coca-Cola 500ml"
                                        value={formData.name}
                                        onChange={(e) => {
                                            setFormData({ ...formData, name: e.target.value });
                                            if (errors.name) setErrors({ ...errors, name: '' });
                                        }}
                                        className={errors.name ? "border-red-500 focus-visible:ring-red-500" : ""}
                                    />
                                    {errors.name && <p className="text-xs text-red-500 dark:text-red-400">{errors.name}</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label>Precio ($) <span className="text-red-500">*</span></Label>
                                    <Input
                                        type="number"
                                        step="0.01"
                                        max={10000000}
                                        placeholder="0.00"
                                        value={formData.price}
                                        onChange={(e) => {
                                            setFormData({ ...formData, price: e.target.value });
                                            if (errors.price) setErrors({ ...errors, price: '' });
                                        }}
                                        className={cn(
                                            "[appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none",
                                            errors.price ? "border-red-500 focus-visible:ring-red-500" : ""
                                        )}
                                    />
                                    {errors.price && <p className="text-xs text-red-500 dark:text-red-400">{errors.price}</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label>{editingId ? 'Stock Actual' : 'Stock Inicial'} <span className="text-red-500">*</span></Label>
                                    <Input
                                        type="number"
                                        max={100000}
                                        placeholder="0"
                                        value={formData.stockQuantity}
                                        onChange={(e) => {
                                            setFormData({ ...formData, stockQuantity: e.target.value });
                                            if (errors.stockQuantity) setErrors({ ...errors, stockQuantity: '' });
                                        }}
                                        className={cn(
                                            "[appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none",
                                            errors.stockQuantity ? "border-red-500 focus-visible:ring-red-500" : ""
                                        )}
                                    />
                                    {errors.stockQuantity && <p className="text-xs text-red-500 dark:text-red-400">{errors.stockQuantity}</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label>Categoría <span className="text-red-500">*</span></Label>
                                    <select
                                        className={cn(
                                            "w-full h-10 px-3 py-2 bg-white dark:bg-slate-900 border rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 dark:text-white transition-colors",
                                            errors.categoryId ? "border-red-500 focus-visible:ring-red-500 ring-red-500 ring-1" : "border-slate-200 dark:border-slate-800"
                                        )}
                                        value={formData.categoryId}
                                        onChange={(e) => {
                                            setFormData({ ...formData, categoryId: e.target.value });
                                            if (errors.categoryId) setErrors({ ...errors, categoryId: '' });
                                        }}
                                    >
                                        <option value="">Seleccionar...</option>
                                        {categories.map(c => (
                                            <option key={c.id} value={c.id}>{c.name}</option>
                                        ))}
                                    </select>
                                    {errors.categoryId && <p className="text-xs text-red-500 dark:text-red-400">{errors.categoryId}</p>}
                                </div>
                                <div className="flex items-end justify-end gap-2 md:col-span-2 lg:col-span-1 pt-2">
                                    <Button
                                        type="button"
                                        variant="outline"
                                        onClick={() => {
                                            setIsAdding(false);
                                            setEditingId(null);
                                            setFormData({ barcode: '', name: '', price: '', stockQuantity: '', categoryId: '' });
                                            setErrors({});
                                        }}
                                    >
                                        Cancelar
                                    </Button>
                                    <Button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white" disabled={submitting}>
                                        {submitting ? 'Guardando...' : (editingId ? 'Actualizar' : 'Crear')}
                                    </Button>
                                </div>
                            </form>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>

            <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                <Table>
                    <TableHeader>
                        <TableRow className="bg-slate-50 dark:bg-slate-800/50 border-slate-100 dark:border-slate-800">
                            <TableHead className="dark:text-slate-400">Producto</TableHead>
                            <TableHead className="dark:text-slate-400">Categoría</TableHead>
                            <TableHead className="dark:text-slate-400">Precio</TableHead>
                            <TableHead className="dark:text-slate-400">Stock</TableHead>
                            {isAdmin && <TableHead className="w-32 dark:text-slate-400">Acciones</TableHead>}
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {products.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={isAdmin ? 5 : 4} className="text-center py-10 text-slate-500">
                                    No hay productos en inventario.
                                </TableCell>
                            </TableRow>
                        ) : (
                            products.map((p) => (
                                <TableRow key={p.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors cursor-default">
                                    <TableCell>
                                        <div className="font-medium dark:text-white">{p.name}</div>
                                        <div className="text-xs text-slate-500 dark:text-slate-400 font-mono flex items-center gap-1">
                                            <Barcode className="h-3 w-3" /> {p.barcode}
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <span className="inline-flex items-center rounded-full bg-slate-100 dark:bg-slate-800 px-2 py-1 text-xs font-medium text-slate-600 dark:text-slate-400 border dark:border-slate-700">
                                            {p.categoryName}
                                        </span>
                                    </TableCell>
                                    <TableCell className="font-semibold text-slate-900 dark:text-white">
                                        {formatCurrency(p.price)}
                                    </TableCell>
                                    <TableCell>
                                        <span className={p.stockQuantity <= 5 ? "text-red-600 dark:text-red-400 font-bold" : "text-slate-600 dark:text-slate-400"}>
                                            {p.stockQuantity} unid.
                                        </span>
                                    </TableCell>
                                    {isAdmin && (
                                        <TableCell>
                                            <div className="flex gap-1">
                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={() => handleEdit(p)}
                                                            className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 p-1 h-8 w-8 transition-colors"
                                                        >
                                                            <Pencil className="h-4 w-4" />
                                                        </Button>
                                                    </TooltipTrigger>
                                                    <TooltipContent>Editar producto</TooltipContent>
                                                </Tooltip>
                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={() => handleDeactivate(p)}
                                                            className="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 p-1 h-8 w-8 transition-colors"
                                                        >
                                                            <Archive className="h-4 w-4" />
                                                        </Button>
                                                    </TooltipTrigger>
                                                    <TooltipContent>Desactivar producto</TooltipContent>
                                                </Tooltip>
                                            </div>
                                        </TableCell>
                                    )}
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {isAdmin && (
                <InactiveProducts reloadSignal={inactiveReloadSignal} onReactivated={fetchProducts} />
            )}
        </div>
    );
}
