'use client';

import { useEffect, useState } from 'react';
import api from '@/lib/api';
import { useCurrentUser } from '@/lib/session';
import { AdminOnly } from '@/components/auth/AdminOnly';
import { isAxiosError } from 'axios';
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
import { Plus, Loader2, UserCheck, UserX } from 'lucide-react';
import { motion } from 'framer-motion';
import { toast } from 'sonner';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';

type Role = 'ADMIN' | 'EMPLOYEE';

interface User {
    id: number;
    username: string;
    email: string;
    role: Role;
    active: boolean;
}

const EMPTY_FORM = { username: '', email: '', password: '', role: 'EMPLOYEE' as Role };

const ROLE_LABELS: Record<Role, string> = {
    ADMIN: 'Administrador',
    EMPLOYEE: 'Empleado',
};

function errorMessage(err: unknown, fallback: string): string {
    if (isAxiosError<{ message?: string }>(err)) {
        return err.response?.data?.message || fallback;
    }
    return fallback;
}

export default function UsersPage() {
    return (
        <AdminOnly>
            <UsersManager />
        </AdminOnly>
    );
}

function UsersManager() {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);

    const [isAdding, setIsAdding] = useState(false);
    const [form, setForm] = useState(EMPTY_FORM);
    const [submitting, setSubmitting] = useState(false);
    const currentUsername = useCurrentUser()?.username;

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            const response = await api.get('/users');
            setUsers(response.data);
        } catch (err) {
            toast.error(errorMessage(err, 'Error al cargar los usuarios'));
        } finally {
            setLoading(false);
        }
    };

    const handleCreateUser = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            await api.post('/users', form);
            toast.success(`Usuario "${form.username}" creado`);
            setForm(EMPTY_FORM);
            setIsAdding(false);
            fetchUsers();
        } catch (err) {
            toast.error(errorMessage(err, 'Error al crear el usuario'));
        } finally {
            setSubmitting(false);
        }
    };

    const handleToggleActive = async (user: User) => {
        const action = user.active ? 'desactivar' : 'activar';
        if (!confirm(`¿Seguro que querés ${action} a "${user.username}"?`)) return;

        try {
            await api.patch(`/users/${user.id}/status`, { active: !user.active });
            toast.success(`Usuario "${user.username}" ${user.active ? 'desactivado' : 'activado'}`);
            fetchUsers();
        } catch (err) {
            toast.error(errorMessage(err, `Error al ${action} el usuario`));
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
                    <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Usuarios</h2>
                    <p className="text-slate-500 dark:text-slate-400">Da de alta empleados y administra el acceso al sistema.</p>
                </div>
                {!isAdding && (
                    <Button onClick={() => setIsAdding(true)} className="bg-orange-600 hover:bg-orange-700">
                        <Plus className="mr-2 h-4 w-4" /> Nuevo Usuario
                    </Button>
                )}
            </div>

            {isAdding && (
                <motion.div
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="bg-white dark:bg-slate-900 p-6 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800"
                >
                    <form onSubmit={handleCreateUser} className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 items-end">
                        <div className="space-y-2">
                            <Label htmlFor="username">Usuario</Label>
                            <Input
                                id="username"
                                value={form.username}
                                onChange={(e) => setForm({ ...form, username: e.target.value })}
                                placeholder="ej. mara_pos"
                                minLength={3}
                                maxLength={20}
                                pattern="[a-zA-Z0-9._\-]+"
                                title="Letras, números, '.', '_' o '-'"
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="email">Email</Label>
                            <Input
                                id="email"
                                type="email"
                                value={form.email}
                                onChange={(e) => setForm({ ...form, email: e.target.value })}
                                placeholder="correo@ejemplo.com"
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="password">Contraseña</Label>
                            <Input
                                id="password"
                                type="password"
                                value={form.password}
                                onChange={(e) => setForm({ ...form, password: e.target.value })}
                                placeholder="Mínimo 8 caracteres"
                                minLength={8}
                                maxLength={72}
                                autoComplete="new-password"
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="role">Rol</Label>
                            <select
                                id="role"
                                className="w-full h-10 px-3 py-2 bg-white dark:bg-slate-800 dark:text-white border border-slate-200 dark:border-slate-700 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-orange-500"
                                value={form.role}
                                onChange={(e) => setForm({ ...form, role: e.target.value as Role })}
                            >
                                <option value="EMPLOYEE">{ROLE_LABELS.EMPLOYEE}</option>
                                <option value="ADMIN">{ROLE_LABELS.ADMIN}</option>
                            </select>
                        </div>
                        <div className="flex gap-2">
                            <Button type="submit" disabled={submitting}>
                                {submitting ? 'Guardando...' : 'Guardar'}
                            </Button>
                            <Button type="button" variant="outline" onClick={() => { setIsAdding(false); setForm(EMPTY_FORM); }}>
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
                            <TableHead className="dark:text-slate-400">Usuario</TableHead>
                            <TableHead className="dark:text-slate-400">Email</TableHead>
                            <TableHead className="dark:text-slate-400">Rol</TableHead>
                            <TableHead className="dark:text-slate-400">Estado</TableHead>
                            <TableHead className="w-16 dark:text-slate-400">Acciones</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {users.map((u) => {
                            const isCurrentUser = u.username === currentUsername;
                            return (
                                <TableRow key={u.id} className="dark:border-slate-800">
                                    <TableCell className="font-bold text-slate-900 dark:text-white">
                                        {u.username}
                                        {isCurrentUser && <span className="ml-2 text-xs font-normal text-slate-400">(vos)</span>}
                                    </TableCell>
                                    <TableCell className="text-slate-600 dark:text-slate-400">{u.email || '-'}</TableCell>
                                    <TableCell>
                                        <span className="px-2 py-0.5 bg-blue-600 text-white text-xs font-bold rounded uppercase tracking-wider">
                                            {ROLE_LABELS[u.role]}
                                        </span>
                                    </TableCell>
                                    <TableCell>
                                        <span className={u.active
                                            ? 'text-emerald-600 dark:text-emerald-400 font-medium'
                                            : 'text-slate-400 font-medium'}>
                                            {u.active ? 'Activo' : 'Inactivo'}
                                        </span>
                                    </TableCell>
                                    <TableCell>
                                        {!isCurrentUser && (
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => handleToggleActive(u)}
                                                        className={`p-1 h-8 w-8 transition-colors ${u.active
                                                            ? 'text-red-600 dark:text-red-400'
                                                            : 'text-emerald-600 dark:text-emerald-400'}`}
                                                    >
                                                        {u.active ? <UserX className="h-4 w-4" /> : <UserCheck className="h-4 w-4" />}
                                                    </Button>
                                                </TooltipTrigger>
                                                <TooltipContent>{u.active ? 'Desactivar usuario' : 'Activar usuario'}</TooltipContent>
                                            </Tooltip>
                                        )}
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}
