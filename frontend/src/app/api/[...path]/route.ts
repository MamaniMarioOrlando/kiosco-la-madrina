import type { NextRequest } from 'next/server';
import {
  clientIpFrom,
  downstreamResponseHeaders,
  upstreamRequestHeaders,
  upstreamUrl,
} from '@/lib/server/backendProxy';

/**
 * Proxy: el navegador llama a /api/... en el mismo dominio del frontend y este código, que corre en el
 * servidor de Next (Vercel), reenvía el pedido al backend (Railway).
 *
 *   navegador ──/api/products──▶ Next (Vercel) ──BACKEND_URL/api/products + secreto + IP──▶ Spring (Railway)
 *
 * BACKEND_URL y PROXY_SECRET se leen en cada pedido y NO llevan el prefijo NEXT_PUBLIC_: nunca llegan al
 * navegador. Sin PROXY_SECRET el proxy funciona igual, pero el backend ve a todos con la IP de Vercel.
 */
export const dynamic = 'force-dynamic';

const DEV_BACKEND_URL = 'http://localhost:8080';

function backendUrl(): string | undefined {
  // En desarrollo hay un default cómodo; en producción, olvidarse la variable tiene que notarse.
  return process.env.BACKEND_URL || (process.env.NODE_ENV === 'production' ? undefined : DEV_BACKEND_URL);
}

async function proxy(request: NextRequest): Promise<Response> {
  const backend = backendUrl();
  if (!backend) {
    console.error('BACKEND_URL no está configurada: el proxy /api no sabe a qué backend llamar.');
    return Response.json({ message: 'El servidor no está configurado.' }, { status: 500 });
  }

  let target: string;
  try {
    target = upstreamUrl(backend, request.nextUrl.pathname, request.nextUrl.search);
  } catch {
    return Response.json({ message: 'Ruta inválida.' }, { status: 404 });
  }

  const hasBody = request.method !== 'GET' && request.method !== 'HEAD';
  try {
    const upstream = await fetch(target, {
      method: request.method,
      headers: upstreamRequestHeaders(request.headers, {
        secret: process.env.PROXY_SECRET,
        clientIp: clientIpFrom(request.headers),
      }),
      body: hasBody ? await request.arrayBuffer() : undefined,
      // Una redirección del backend se le devuelve al navegador tal cual; no se sigue acá.
      redirect: 'manual',
      cache: 'no-store',
    });

    return new Response(upstream.body, {
      status: upstream.status,
      statusText: upstream.statusText,
      headers: downstreamResponseHeaders(upstream.headers),
    });
  } catch (error) {
    // El backend está caído o dormido (Railway con App Sleeping): 502 = "el de atrás no contestó".
    console.error(`Proxy /api: no se pudo llegar a ${target}`, error);
    return Response.json({ message: 'No se pudo conectar con el servidor. Probá de nuevo.' }, { status: 502 });
  }
}

export { proxy as DELETE, proxy as GET, proxy as PATCH, proxy as POST, proxy as PUT };
