import { isAxiosError } from 'axios';

/**
 * Mensaje para mostrarle al usuario a partir de un error de la API.
 * El backend responde siempre { message } en español; si no hay respuesta (red caída) o el
 * formato es otro, se usa el texto de respaldo.
 */
export function apiErrorMessage(err: unknown, fallback: string): string {
    if (isAxiosError<{ message?: string }>(err)) {
        return err.response?.data?.message || fallback;
    }
    return fallback;
}
