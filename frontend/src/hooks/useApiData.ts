'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';
import { apiErrorMessage } from '@/lib/errors';

interface UseApiDataOptions {
    /** Texto del aviso si falla la carga. Sin él, el error solo va a la consola (chequeos en segundo plano). */
    errorMessage?: string;
    /** Si se indica, vuelve a pedir los datos cada tantos milisegundos. */
    refreshMs?: number;
    /** Cada vez que cambia, vuelve a pedir los datos (p. ej. al navegar o por una señal del componente padre). */
    refreshKey?: unknown;
}

/**
 * Datos de la API ligados a la vida del componente: los pide al montarse, avisa si fallan y
 * expone reload() para refrescarlos después de un cambio. Reúne el useState + useEffect + try/catch
 * que antes repetía cada pantalla.
 *
 * Cada componente que lo usa tiene su propia copia de los datos: un hook comparte lógica, no estado.
 */
export function useApiData<T>(fetcher: () => Promise<T>, initialData: T, options: UseApiDataOptions = {}) {
    const { errorMessage, refreshMs, refreshKey } = options;
    const [data, setData] = useState<T>(initialData);
    const [loading, setLoading] = useState(true);

    // Siempre la última versión de fetcher, sin que sea dependencia de los efectos: si quien llama
    // pasa una función nueva en cada render (() => ...), no se dispara un pedido por render.
    const fetcherRef = useRef(fetcher);
    useEffect(() => {
        fetcherRef.current = fetcher;
    });

    const reload = useCallback(async () => {
        try {
            setData(await fetcherRef.current());
        } catch (err) {
            if (errorMessage) {
                toast.error(apiErrorMessage(err, errorMessage));
            } else {
                console.error(err);
            }
        } finally {
            setLoading(false);
        }
    }, [errorMessage]);

    useEffect(() => {
        reload();
        if (!refreshMs) return;
        const interval = setInterval(reload, refreshMs);
        // Limpieza: al desmontar (o si cambian las opciones) se corta el intervalo anterior.
        return () => clearInterval(interval);
    }, [reload, refreshMs, refreshKey]);

    return { data, loading, reload };
}
