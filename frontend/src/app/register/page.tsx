'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Lock, User, Mail, ShieldCheck } from 'lucide-react';
import api from '@/lib/api';
import { motion } from 'framer-motion';
import Link from 'next/link';

export default function RegisterPage() {
    const router = useRouter();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        role: 'employee'
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await api.post('/auth/signup', {
                ...formData,
                role: [formData.role] // Backend expects a Set/List of strings
            });
            setSuccess(true);
            setTimeout(() => {
                router.push('/login');
            }, 2000);
        } catch (err: any) {
            console.error('Registration failed', err);
            const backendMessage = err.response?.data?.message || err.response?.data;
            setError(typeof backendMessage === 'string' ? backendMessage : 'Error al registrar usuario. Verifica los datos.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4">
            <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                className="w-full max-w-md"
            >
                <Card className="shadow-xl border-t-4 border-t-orange-600">
                    <CardHeader className="space-y-1">
                        <CardTitle className="text-2xl font-bold text-center text-slate-900">Crear Cuenta</CardTitle>
                        <CardDescription className="text-center">
                            Registra un nuevo usuario para el sistema
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        {success ? (
                            <div className="bg-green-100 text-green-800 p-4 rounded-lg text-center font-medium animate-pulse">
                                ¡Registro exitoso! Redirigiendo al login...
                            </div>
                        ) : (
                            <form onSubmit={handleSubmit} className="space-y-4">
                                <div className="space-y-2">
                                    <Label htmlFor="username">Usuario</Label>
                                    <div className="relative">
                                        <User className="absolute left-3 top-2.5 h-5 w-5 text-slate-400" />
                                        <Input
                                            id="username"
                                            placeholder="ej. mara_pos"
                                            value={formData.username}
                                            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                            className="pl-10"
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="email">Email</Label>
                                    <div className="relative">
                                        <Mail className="absolute left-3 top-2.5 h-5 w-5 text-slate-400" />
                                        <Input
                                            id="email"
                                            type="email"
                                            placeholder="correo@ejemplo.com"
                                            value={formData.email}
                                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                            className="pl-10"
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="password">Contraseña</Label>
                                    <div className="relative">
                                        <Lock className="absolute left-3 top-2.5 h-5 w-5 text-slate-400" />
                                        <Input
                                            id="password"
                                            type="password"
                                            value={formData.password}
                                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                                            className="pl-10"
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="role">Rol en el Sistema</Label>
                                    <div className="relative">
                                        <ShieldCheck className="absolute left-3 top-2.5 h-5 w-5 text-slate-400" />
                                        <select
                                            id="role"
                                            className="w-full h-10 pl-10 pr-3 py-2 bg-white border border-slate-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-orange-500"
                                            value={formData.role}
                                            onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                                        >
                                            <option value="employee">Empleado (Ventas)</option>
                                            <option value="admin">Administrador (Stock y Reportes)</option>
                                        </select>
                                    </div>
                                </div>
                                {error && (
                                    <div className="text-sm text-red-500 font-medium text-center bg-red-50 p-2 rounded">
                                        {error}
                                    </div>
                                )}
                                <Button type="submit" className="w-full bg-orange-600 hover:bg-orange-700" disabled={loading}>
                                    {loading ? 'Registrando...' : 'Registrar Usuario'}
                                </Button>
                            </form>
                        )}
                    </CardContent>
                    <CardFooter className="flex flex-col space-y-2">
                        <Link href="/login" className="text-sm text-orange-600 hover:underline">
                            ¿Ya tienes cuenta? Ingresa aquí
                        </Link>
                    </CardFooter>
                </Card>
            </motion.div>
        </div>
    );
}
