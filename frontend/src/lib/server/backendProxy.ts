/**
 * Piezas puras del proxy /api → backend (src/app/api/[...path]/route.ts). Sin Next ni process.env adentro,
 * para poder testearlas con Vitest.
 *
 * Por qué hay proxy: el navegador le habla solo a su propio dominio (Vercel) y Next reenvía a Railway.
 * Así una cookie de sesión es "de primera parte" y el backend deja de ser llamado desde el navegador.
 */

/** Los mismos nombres que TrustedProxyFilter en el backend. */
export const SECRET_HEADER = 'x-kiosco-proxy-secret';
export const CLIENT_IP_HEADER = 'x-kiosco-client-ip';

const API_PREFIX = '/api/';

/**
 * Lista blanca: solo viaja lo que la API necesita. Una lista negra habría que mantenerla al día con cada
 * encabezado nuevo de Vercel o del navegador; esta falla "cerrada".
 * Origin no está: la llamada ahora es de servidor a servidor, y el CORS del backend no aplica.
 */
const FORWARDED_REQUEST_HEADERS = ['accept', 'accept-language', 'authorization', 'content-type', 'cookie'];

/**
 * Encabezados que describen la conexión o la codificación de la respuesta original, no el contenido:
 * fetch ya descomprimió el cuerpo y Next arma su propia conexión con el navegador.
 */
const DROPPED_RESPONSE_HEADERS = new Set([
  'connection',
  'content-encoding',
  'content-length',
  'keep-alive',
  'transfer-encoding',
]);

export function upstreamUrl(backendUrl: string, pathname: string, search: string): string {
  if (!pathname.startsWith(API_PREFIX)) {
    throw new Error(`Solo se reenvían rutas bajo ${API_PREFIX}: ${pathname}`);
  }
  const base = new URL(backendUrl);
  const target = new URL(pathname + search, base);
  if (target.origin !== base.origin) {
    throw new Error(`La ruta ${pathname} apunta a otro servidor`);
  }
  return target.toString();
}

/**
 * La IP del navegador según la plataforma. Vercel sobrescribe X-Forwarded-For con la IP real del cliente
 * (no confía en lo que mande el navegador), así que la primera dirección es la correcta.
 */
export function clientIpFrom(headers: Headers): string | undefined {
  const forwarded = headers.get('x-forwarded-for')?.split(',')[0]?.trim();
  const ip = forwarded || headers.get('x-real-ip')?.trim();
  return ip || undefined;
}

export function upstreamRequestHeaders(
  incoming: Headers,
  { secret, clientIp }: { secret?: string; clientIp?: string },
): Headers {
  const headers = new Headers();
  for (const name of FORWARDED_REQUEST_HEADERS) {
    const value = incoming.get(name);
    if (value !== null) {
      headers.set(name, value);
    }
  }
  if (secret) {
    headers.set(SECRET_HEADER, secret);
    if (clientIp) {
      headers.set(CLIENT_IP_HEADER, clientIp);
    }
  }
  return headers;
}

export function downstreamResponseHeaders(upstream: Headers): Headers {
  const headers = new Headers();
  upstream.forEach((value, name) => {
    if (name !== 'set-cookie' && !DROPPED_RESPONSE_HEADERS.has(name)) {
      headers.set(name, value);
    }
  });
  // Cada cookie va en su propio Set-Cookie: unirlas con comas (lo que hace forEach) las rompería.
  for (const cookie of upstream.getSetCookie()) {
    headers.append('set-cookie', cookie);
  }
  return headers;
}
