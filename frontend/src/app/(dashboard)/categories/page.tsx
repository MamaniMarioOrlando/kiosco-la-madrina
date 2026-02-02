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
import { Plus, Loader2, Pencil, Trash2, X, Check } from 'lucide-react';
import { motion } from 'framer-motion';

interface Category {
    id: number;
    name: string;
    description: string;
}

export default function CategoriesPage() {
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Form state
    const [isAdding, setIsAdding] = useState(false);
    const [newName, setNewName] = useState('');
    const [newDesc, setNewDesc] = useState('');
    const [submitting, setSubmitting] = useState(false);

    // Edit state
    const [editingId, setEditingId] = useState<number | null>(null);
    const [editName, setEditName] = useState('');
    const [editDesc, setEditDesc] = useState('');

    const [isAdmin, setIsAdmin] = useState(false);

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            const user = JSON.parse(userData);
            setIsAdmin(user.roles?.some((r: string) => r.toLowerCase().includes('admin')));
        }
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        try {
            const response = await api.get('/categories');
            setCategories(response.data);
        } catch (err: any) {
            setError('Error al cargar categorías');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddCategory = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            await api.post('/categories', {
                name: newName,
                description: newDesc
            });
            setNewName('');
            setNewDesc('');
            setIsAdding(false);
            fetchCategories();
        } catch (err: any) {
            console.error('Detalles del error:', err);
            const status = err.response?.status;
            const message = err.response?.data?.message || err.response?.data || err.message;

            if (status === 403) {
                alert('Error 403: No tienes permisos de ADMIN (Forbidden).');
            } else if (status === 401) {
                alert('Error 401: No autorizado. Sesión inválida o expirada.');
            } else {
                alert(`Error al crear categoría: ${status || '?'}. ${message}`);
            }
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeleteCategory = async (id: number) => {
        if (!confirm('¿Estás seguro de eliminar esta categoría? Se eliminarán los productos asociados.')) return;

        try {
            await api.delete(`/categories/${id}`);
            fetchCategories();
        } catch (err: any) {
            console.error(err);
            alert('Error al eliminar la categoría');
        }
    };

    const handleEditClick = (category: Category) => {
        setEditingId(category.id);
        setEditName(category.name);
        setEditDesc(category.description || '');
    };

    const handleUpdateCategory = async (id: number) => {
        setSubmitting(true);
        try {
            await api.put(`/categories/${id}`, {
                name: editName,
                description: editDesc
            });
            setEditingId(null);
            fetchCategories();
        } catch (err: any) {
            console.error(err);
            alert('Error al actualizar la categoría');
        } finally {
            setSubmitting(false);
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
                    <h2 className="text-2xl font-bold text-slate-900">Categorías</h2>
                    <p className="text-slate-500">Gestiona las categorías de tus productos.</p>
                </div>
                {isAdmin && !isAdding && (
                    <Button onClick={() => setIsAdding(true)} className="bg-orange-600 hover:bg-orange-700">
                        <Plus className="mr-2 h-4 w-4" /> Nueva Categoría
                    </Button>
                )}
            </div>

            {isAdmin && isAdding && (
                <motion.div
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="bg-white p-6 rounded-xl shadow-sm border border-orange-100"
                >
                    <form onSubmit={handleAddCategory} className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
                        <div className="space-y-2">
                            <Label htmlFor="name">Nombre</Label>
                            <Input
                                id="name"
                                value={newName}
                                onChange={(e) => setNewName(e.target.value)}
                                placeholder="Ej: Bebidas"
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="desc">Descripción</Label>
                            <Input
                                id="desc"
                                value={newDesc}
                                onChange={(e) => setNewDesc(e.target.value)}
                                placeholder="Opcional"
                            />
                        </div>
                        <div className="flex gap-2">
                            <Button type="submit" disabled={submitting}>
                                {submitting ? 'Guardando...' : 'Guardar'}
                            </Button>
                            <Button type="button" variant="outline" onClick={() => setIsAdding(false)}>
                                Cancelar
                            </Button>
                        </div>
                    </form>
                </motion.div>
            )}

            <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-100 dark:border-slate-800 overflow-hidden">
                <Table>
                    <TableHeader>
                        <TableRow className="bg-slate-50 dark:bg-slate-800/50 border-slate-100 dark:border-slate-800">
                            <TableHead className="dark:text-slate-400">Nombre</TableHead>
                            <TableHead className="dark:text-slate-400">Descripción</TableHead>
                            {isAdmin && <TableHead className="w-16 dark:text-slate-400">Acciones</TableHead>}
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {categories.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={isAdmin ? 3 : 2} className="text-center py-10 text-slate-500">
                                    No hay categorías registradas.
                                </TableCell>
                            </TableRow>
                        ) : (
                            categories.map((c) => (
                                <TableRow key={c.id} className="dark:border-slate-800">
                                    <TableCell className="font-bold text-slate-900 dark:text-white">
                                        {editingId === c.id ? (
                                            <Input value={editName} onChange={(e) => setEditName(e.target.value)} size={1} className="h-8 font-bold" />
                                        ) : (
                                            c.name
                                        )}
                                    </TableCell>
                                    <TableCell className="text-slate-600 dark:text-slate-400">
                                        {editingId === c.id ? (
                                            <Input value={editDesc} onChange={(e) => setEditDesc(e.target.value)} size={1} className="h-8" />
                                        ) : (
                                            c.description || '-'
                                        )}
                                    </TableCell>
                                    {isAdmin && (
                                        <TableCell>
                                            <div className="flex gap-2">
                                                {editingId === c.id ? (
                                                    <>
                                                        <Button variant="ghost" size="sm" onClick={() => handleUpdateCategory(c.id)} className="text-green-600 p-1 h-8 w-8">
                                                            <Check className="h-4 w-4" />
                                                        </Button>
                                                        <Button variant="ghost" size="sm" onClick={() => setEditingId(null)} className="text-red-600 p-1 h-8 w-8">
                                                            <X className="h-4 w-4" />
                                                        </Button>
                                                    </>
                                                ) : (
                                                    <>
                                                        <Button variant="ghost" size="sm" onClick={() => handleEditClick(c)} className="text-blue-600 p-1 h-8 w-8">
                                                            <Pencil className="h-4 w-4" />
                                                        </Button>
                                                        <Button variant="ghost" size="sm" onClick={() => handleDeleteCategory(c.id)} className="text-red-600 p-1 h-8 w-8">
                                                            <Trash2 className="h-4 w-4" />
                                                        </Button>
                                                    </>
                                                )}
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
