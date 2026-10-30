'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useSessionStatus } from '@/lib/session';
import { FullScreenSpinner } from './FullScreenSpinner';

/**
 * El espejo de AuthGuard, para las pantallas de invitado como el login: con sesión, manda al panel.
 *
 * Sin este guard, alguien logueado que llega al login (con "atrás" o escribiendo la URL) ve el
 * formulario y cree que se le cerró la sesión, aunque el token siga guardado.
 */
export function GuestGuard({ children }: { children: React.ReactNode }) {
    const status = useSessionStatus();
    const router = useRouter();

    useEffect(() => {
        if (status === 'authenticated') router.replace('/');
    }, [status, router]);

    if (status !== 'anonymous') return <FullScreenSpinner />;

    return <>{children}</>;
}
