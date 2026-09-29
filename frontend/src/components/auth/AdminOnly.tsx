'use client';

import Link from 'next/link';
import { Loader2, ShieldAlert } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { isAdmin, useCurrentUser } from '@/lib/session';

/**
 * Renderiza su contenido solo para administradores.
 *
 * Es una mejora de experiencia, no la barrera de seguridad: esa la pone el backend (403).
 * Evita mostrarle a un empleado una pantalla de admin que igual fallaría al usarla.
 * Como el contenido no se monta, tampoco dispara peticiones que el backend rechazaría.
 */
export function AdminOnly({ children }: { children: React.ReactNode }) {
    const user = useCurrentUser();

    if (!user) {
        return (
            <div className="flex h-full items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
            </div>
        );
    }

    if (!isAdmin(user)) {
        return (
            <div className="flex flex-col items-center justify-center text-center py-20 gap-4">
                <div className="h-16 w-16 rounded-full bg-red-100 dark:bg-red-950/40 flex items-center justify-center">
                    <ShieldAlert className="h-8 w-8 text-red-600 dark:text-red-400" />
                </div>
                <div>
                    <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">Acceso restringido</h2>
                    <p className="text-slate-500 dark:text-slate-400 mt-1">
                        Esta sección es solo para administradores.
                    </p>
                </div>
                <Button asChild variant="outline">
                    <Link href="/">Volver al Dashboard</Link>
                </Button>
            </div>
        );
    }

    return <>{children}</>;
}
