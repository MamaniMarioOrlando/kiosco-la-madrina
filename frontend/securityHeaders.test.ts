import { describe, expect, it } from 'vitest';
import { buildContentSecurityPolicy, buildSecurityHeaders } from './securityHeaders';

const PROD = { apiUrl: 'https://kiosco-api.example/api', isDev: false };
const DEV = { apiUrl: 'http://localhost:8080/api', isDev: true };

/** "a x y; b z" → { a: ['x', 'y'], b: ['z'] } */
function directives(policy: string): Record<string, string[]> {
  return Object.fromEntries(
    policy.split('; ').map((directive) => {
      const [name, ...values] = directive.split(' ');
      return [name, values];
    }),
  );
}

describe('buildContentSecurityPolicy', () => {
  it('lets the browser talk only to its own origin and the API origin (without the path)', () => {
    expect(directives(buildContentSecurityPolicy(PROD))['connect-src']).toEqual([
      "'self'",
      'https://kiosco-api.example',
    ]);
  });

  it('never allows eval in production', () => {
    expect(directives(buildContentSecurityPolicy(PROD))['script-src']).not.toContain("'unsafe-eval'");
  });

  it('adds what React and hot reload need in development', () => {
    const csp = directives(buildContentSecurityPolicy(DEV));
    expect(csp['script-src']).toContain("'unsafe-eval'");
    expect(csp['connect-src']).toContain('ws:');
  });

  it('forbids framing, plugins and foreign scripts', () => {
    const csp = directives(buildContentSecurityPolicy(PROD));
    expect(csp['frame-ancestors']).toEqual(["'none'"]);
    expect(csp['object-src']).toEqual(["'none'"]);
    expect(csp['script-src']).toEqual(["'self'", "'unsafe-inline'"]);
  });

  it('allows data: images, because avatars are stored as base64', () => {
    expect(directives(buildContentSecurityPolicy(PROD))['img-src']).toContain('data:');
  });

  it('fails the build when the API URL is not a valid URL', () => {
    expect(() => buildContentSecurityPolicy({ apiUrl: 'kiosco-api', isDev: false })).toThrow();
  });
});

describe('buildSecurityHeaders', () => {
  const names = (options: typeof PROD) => buildSecurityHeaders(options).map((header) => header.key);

  it('sends HSTS only in production, where the site is served over https', () => {
    expect(names(PROD)).toContain('Strict-Transport-Security');
    expect(names(DEV)).not.toContain('Strict-Transport-Security');
  });

  it('always sends the baseline headers, each once', () => {
    for (const options of [PROD, DEV]) {
      expect(names(options)).toEqual(
        expect.arrayContaining([
          'Content-Security-Policy',
          'X-Content-Type-Options',
          'X-Frame-Options',
          'Referrer-Policy',
          'Permissions-Policy',
        ]),
      );
      expect(new Set(names(options)).size).toBe(names(options).length);
    }
  });
});
