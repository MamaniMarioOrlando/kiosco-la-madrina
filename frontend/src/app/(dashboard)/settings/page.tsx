'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Settings, User, Shield, LogOut } from 'lucide-react';
import { motion } from 'framer-motion';

export default function SettingsPage() {
    const router = useRouter();
    const [user, setUser] = useState<any>(null);

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            setUser(JSON.parse(userData));
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        router.push('/login');
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center gap-2">
                <Settings className="h-6 w-6 text-slate-600" />
                <h2 className="text-2xl font-bold text-slate-900">Configuración</h2>
            </div>

            <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="max-w-2xl"
            >
                <Card>
                    <CardHeader>
                        <CardTitle>Perfil de Usuario</CardTitle>
                        <CardDescription>Información del usuario actualmente autenticado.</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="flex items-center gap-4 p-4 bg-slate-50 rounded-lg">
                            <div className="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center">
                                <User className="h-6 w-6 text-orange-600" />
                            </div>
                            <div>
                                <p className="text-sm font-medium text-slate-500">Nombre de Usuario</p>
                                <p className="text-lg font-bold text-slate-900">{user?.username || 'Cargando...'}</p>
                            </div>
                        </div>

                        <div className="flex items-center gap-4 p-4 bg-slate-50 rounded-lg">
                            <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center">
                                <Shield className="h-6 w-6 text-blue-600" />
                            </div>
                            <div>
                                <p className="text-sm font-medium text-slate-500">Rol asignado</p>
                                <div className="flex gap-2 mt-1">
                                    {user?.roles?.map((role: string) => (
                                        <span
                                            key={role}
                                            className="px-2 py-0.5 bg-blue-600 text-white text-xs font-bold rounded uppercase"
                                        >
                                            {role}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </CardContent>
                    <CardFooter className="bg-slate-50/50 flex justify-end">
                        <Button
                            variant="destructive"
                            onClick={handleLogout}
                            className="flex items-center gap-2"
                        >
                            <LogOut className="h-4 w-4" /> Cerrar Sesión
                        </Button>
                    </CardFooter>
                </Card>

                <div className="mt-8 p-6 border border-dashed border-slate-300 rounded-xl bg-slate-50/50">
                    <h3 className="text-sm font-bold text-slate-400 uppercase tracking-widest mb-2">Próximamente</h3>
                    <p className="text-sm text-slate-500">
                        Próximamente podrás configurar el nombre del Kiosco, el logo y los ticket de impresión desde aquí.
                    </p>
                </div>
            </motion.div>
        </div>
    );
}
