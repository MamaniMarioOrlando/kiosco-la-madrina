'use client';

import { useEffect, useState } from 'react';
import api from '@/lib/api';
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
import { Plus, Loader2, Package, Barcode, Trash2, Pencil } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { formatCurrency } from '@/lib/utils';

interface Product {
    id: number;
    barcode: string;
    name: string;
    price: number;
    stockQuantity: number;
    categoryId: number;
    categoryName: string;
}

interface Category {
    id: number;
    name: string;
}

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
    const [submitting, setSubmitting] = useState(false);

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
            const response = await api.get('/products');
            setProducts(response.data);
        } catch (err) {
            console.error('Error fetching products', err);
        }
    };

    const fetchCategories = async () => {
        try {
            const response = await api.get('/categories');
            setCategories(response.data);
        } catch (err) {
            console.error('Error fetching categories', err);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const payload = {
                ...formData,
                price: parseFloat(formData.price),
                stockQuantity: parseInt(formData.stockQuantity),
                categoryId: parseInt(formData.categoryId)
            };

            if (editingId) {
                await api.put(`/products/${editingId}`, payload);
            } else {
                await api.post('/products', payload);
            }

            setIsAdding(false);
            setEditingId(null);
            setFormData({ barcode: '', name: '', price: '', stockQuantity: '', categoryId: '' });
            fetchProducts();
        } catch (err: any) {
            console.error('Detalles del error:', err);
            const status = err.response?.status;
            const message = err.response?.data?.message || err.response?.data || err.message;

            if (status === 403) {
                alert('Error 403: No tienes permisos de ADMIN (Forbidden).');
            } else if (status === 401) {
                alert('Error 401: No autorizado. Sesión inválida o expirada.');
            } else {
                alert(`Error al ${editingId ? 'actualizar' : 'crear'} producto: ${status || '?'}. ${message}`);
            }
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
            categoryId: p.categoryId.toString()
        });
        setIsAdding(true);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const handleDelete = async (id: number) => {
        if (!confirm('¿Estás seguro de eliminar este producto?')) return;
        try {
            await api.delete(`/products/${id}`);
            fetchProducts();
        } catch (err) {
            console.error(err);
            alert('No se pudo eliminar el producto.');
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
                    <h2 className="text-2xl font-bold text-slate-900">Productos</h2>
                    <p className="text-slate-500">Administra el inventario y precios.</p>
                </div>
                {isAdmin && (
                    <Button
                        onClick={() => {
                            if (isAdding) {
                                setIsAdding(false);
                                setEditingId(null);
                                setFormData({ barcode: '', name: '', price: '', stockQuantity: '', categoryId: '' });
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
                        <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200 mb-6">
                            <h3 className="text-lg font-semibold mb-4 text-slate-800">
                                {editingId ? 'Editar Producto' : 'Nuevo Producto'}
                            </h3>
                            <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                <div className="space-y-2">
                                    <Label>Código de Barras</Label>
                                    <div className="relative">
                                        <Barcode className="absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
                                        <Input
                                            className="pl-9"
                                            value={formData.barcode}
                                            onChange={(e) => setFormData({ ...formData, barcode: e.target.value })}
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <Label>Nombre</Label>
                                    <Input
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label>Precio ($)</Label>
                                    <Input
                                        type="number"
                                        step="0.01"
                                        value={formData.price}
                                        onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label>{editingId ? 'Stock Actual' : 'Stock Inicial'}</Label>
                                    <Input
                                        type="number"
                                        value={formData.stockQuantity}
                                        onChange={(e) => setFormData({ ...formData, stockQuantity: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label>Categoría</Label>
                                    <select
                                        className="w-full h-10 px-3 py-2 bg-white border border-slate-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-orange-500"
                                        value={formData.categoryId}
                                        onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                                        required
                                    >
                                        <option value="">Seleccionar...</option>
                                        {categories.map(c => (
                                            <option key={c.id} value={c.id}>{c.name}</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="flex items-end gap-2">
                                    <Button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700" disabled={submitting}>
                                        {submitting ? 'Guardando...' : (editingId ? 'Actualizar Producto' : 'Crear Producto')}
                                    </Button>
                                    <Button
                                        type="button"
                                        variant="outline"
                                        onClick={() => {
                                            setIsAdding(false);
                                            setEditingId(null);
                                            setFormData({ barcode: '', name: '', price: '', stockQuantity: '', categoryId: '' });
                                        }}
                                    >
                                        Cancelar
                                    </Button>
                                </div>
                            </form>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>

            <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-100 dark:border-slate-800 overflow-hidden">
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
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleEdit(p)}
                                                    className="text-blue-600 hover:text-blue-700 p-1 h-8 w-8"
                                                >
                                                    <Pencil className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleDelete(p.id)}
                                                    className="text-red-600 hover:text-red-700 p-1 h-8 w-8"
                                                >
                                                    <Trash2 className="h-4 w-4" />
                                                </Button>
                                            </div>
                                        </TableCell>
                                    )}
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}
