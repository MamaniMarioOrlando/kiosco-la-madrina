'use client';

import { useRouter } from 'next/navigation';
import { clearSession } from '@/lib/session';
import { authService } from '@/services/authService';

/**
 * Cerrar sesión desde cualquier pantalla (menú del usuario, Configuración) de una sola manera.
 *
 * Primero el backend borra la cookie httpOnly: desde JavaScript no se puede. Si ese pedido falla (sin
 * conexión, sesión ya vencida), igual se limpia la pantalla: el usuario pidió salir, no se lo deja adentro.
 */
export function useLogout() {
    const router = useRouter();

    return async () => {
        try {
            await authService.logout();
        } catch {
            // La cookie vence sola; lo importante es que la pantalla deje de mostrar la sesión.
        } finally {
            clearSession();
            // replace: "atrás" no vuelve a una pantalla de la sesión cerrada.
            router.replace('/login');
        }
    };
}
