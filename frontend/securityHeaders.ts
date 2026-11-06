/**
 * Headers HTTP de seguridad que Next.js agrega a cada respuesta del frontend.
 *
 * Es una función pura (sin leer process.env adentro) para poder testearla
 * sin levantar Next: recibe todo lo que depende del entorno.
 */
export interface SecurityHeadersOptions {
  /** URL de la API (NEXT_PUBLIC_API_URL). Solo se usa su origen para connect-src. */
  apiUrl: string;
  /** En desarrollo, React y el hot reload necesitan eval y WebSocket. */
  isDev: boolean;
}

export function buildContentSecurityPolicy({ apiUrl, isDev }: SecurityHeadersOptions): string {
  const apiOrigin = new URL(apiUrl).origin;

  const directives: Record<string, string[]> = {
    'default-src': ["'self'"],
    // 'unsafe-inline': Next.js hidrata con scripts inline y el tema oscuro se aplica con uno.
    // Deuda conocida: la alternativa estricta es un nonce por pedido (proxy.ts), que obliga
    // a renderizar todas las páginas de forma dinámica.
    'script-src': ["'self'", "'unsafe-inline'", ...(isDev ? ["'unsafe-eval'"] : [])],
    // framer-motion y sonner escriben estilos inline.
    'style-src': ["'self'", "'unsafe-inline'"],
    // data: para los avatares, que se guardan como imagen en base64.
    'img-src': ["'self'", 'data:', 'blob:'],
    // next/font descarga las fuentes en el build y las sirve desde el mismo dominio.
    'font-src': ["'self'"],
    // La única API a la que el navegador puede hablar: un XSS no puede mandar datos a otro dominio.
    'connect-src': ["'self'", apiOrigin, ...(isDev ? ['ws:'] : [])],
    'object-src': ["'none'"],
    'base-uri': ["'self'"],
    'form-action': ["'self'"],
    // Nadie puede meter el kiosco en un <iframe> (clickjacking).
    'frame-ancestors': ["'none'"],
  };

  return Object.entries(directives)
    .map(([name, values]) => `${name} ${values.join(' ')}`)
    .join('; ');
}

export function buildSecurityHeaders(options: SecurityHeadersOptions): { key: string; value: string }[] {
  const headers = [
    { key: 'Content-Security-Policy', value: buildContentSecurityPolicy(options) },
    // El navegador no adivina el tipo de un archivo: un .txt subido no se ejecuta como script.
    { key: 'X-Content-Type-Options', value: 'nosniff' },
    // Equivalente viejo de frame-ancestors, para navegadores que no leen CSP.
    { key: 'X-Frame-Options', value: 'DENY' },
    // A otros sitios solo se les manda el dominio, nunca la ruta completa.
    { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
    // El kiosco no usa cámara, micrófono ni ubicación: se bloquean aunque un script los pida.
    { key: 'Permissions-Policy', value: 'camera=(), microphone=(), geolocation=()' },
  ];

  if (!options.isDev) {
    // HTTPS obligatorio por 2 años. En local se omite: localhost corre en http.
    headers.push({ key: 'Strict-Transport-Security', value: 'max-age=63072000; includeSubDomains' });
  }
  return headers;
}
