'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Lock, User } from 'lucide-react';
import api from '@/lib/api';
import { isAxiosError } from 'axios';
import { startSession } from '@/lib/session';
import { motion } from 'framer-motion';

export default function LoginPage() {
    const router = useRouter();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await api.post('/auth/signin', {
                username,
                password,
            });

            const { token, roles, username: user, id, avatarUrl } = response.data;

            startSession(token, { username: user, roles, id, avatarUrl });

            router.push('/');
        } catch (err) {
            // Sin console.error: un 401 acá es un caso esperado (credenciales inválidas), no un bug.
            if (isAxiosError(err) && err.response?.status === 401) {
                setError('Usuario o contraseña incorrectos');
            } else if (isAxiosError(err) && err.response) {
                setError('Error en el servidor. Intentá de nuevo en unos minutos.');
            } else {
                setError('No se pudo conectar con el servidor');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 dark:bg-slate-950 p-4">
            <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
                className="w-full max-w-md"
            >
                <Card className="shadow-lg border-t-4 border-t-indigo-600 bg-white dark:bg-slate-900 dark:border-slate-800">
                    <CardHeader className="space-y-1">
                        <CardTitle className="text-2xl font-bold text-center text-slate-900 dark:text-white">Kiosco La Madrina</CardTitle>
                        <CardDescription className="text-center dark:text-slate-400">
                            Ingresa tus credenciales para acceder al sistema
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <form onSubmit={handleLogin} className="space-y-4">
                            <div className="space-y-2">
                                <Label htmlFor="username" className="dark:text-slate-300">Usuario</Label>
                                <div className="relative">
                                    <User className="absolute left-3 top-2.5 h-5 w-5 text-slate-400" />
                                    <Input
                                        id="username"
                                        placeholder="ej. admin"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                        className="pl-10"
                                        required
                                    />
                                </div>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="password" className="dark:text-slate-300">Contraseña</Label>
                                <div className="relative">
                                    <Lock className="absolute left-3 top-2.5 h-5 w-5 text-slate-400" />
                                    <Input
                                        id="password"
                                        type="password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="pl-10"
                                        required
                                    />
                                </div>
                            </div>
                            {error && (
                                <div className="text-sm text-red-500 font-medium text-center">
                                    {error}
                                </div>
                            )}
                            <Button type="submit" className="w-full bg-indigo-600 hover:bg-indigo-700" disabled={loading}>
                                {loading ? 'Ingresando...' : 'Ingresar'}
                            </Button>
                        </form>
                    </CardContent>
                    <CardFooter className="flex flex-col space-y-2">
                        <p className="text-sm text-slate-500 dark:text-slate-400">
                            ¿No tenés usuario? Pedíselo al administrador.
                        </p>
                        <p className="text-xs text-slate-500 dark:text-slate-500">
                            Sistema de Gestión v1.0
                        </p>
                    </CardFooter>
                </Card>
            </motion.div>
        </div>
    );
}
