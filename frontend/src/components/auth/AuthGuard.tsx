'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import { useSessionStatus } from '@/lib/session';

/**
 * Protege todas las pantallas del panel: sin sesión, manda al login antes de dibujar nada.
 *
 * Igual que AdminOnly, es experiencia de usuario y no seguridad: la barrera real es el backend,
 * que rechaza con 401 cualquier pedido sin un token válido. El guard evita mostrar una pantalla
 * vacía y disparar pedidos que ya se sabe que van a fallar.
 */
export function AuthGuard({ children }: { children: React.ReactNode }) {
    const status = useSessionStatus();
    const router = useRouter();

    useEffect(() => {
        // replace y no push: el botón "atrás" no debe volver a una pantalla protegida.
        if (status === 'anonymous') router.replace('/login');
    }, [status, router]);

    if (status !== 'authenticated') {
        return (
            <div className="flex min-h-screen items-center justify-center bg-slate-50 dark:bg-slate-950">
                <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
            </div>
        );
    }

    return <>{children}</>;
}
