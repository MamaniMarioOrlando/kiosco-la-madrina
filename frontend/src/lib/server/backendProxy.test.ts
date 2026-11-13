import { describe, expect, it } from 'vitest';
import {
  CLIENT_IP_HEADER,
  SECRET_HEADER,
  clientIpFrom,
  downstreamResponseHeaders,
  upstreamRequestHeaders,
  upstreamUrl,
} from './backendProxy';

const BACKEND = 'https://kiosco-api.example';

describe('upstreamUrl', () => {
  it('keeps the /api path and the query string', () => {
    expect(upstreamUrl(BACKEND, '/api/sales/history', '?page=2&size=20')).toBe(
      'https://kiosco-api.example/api/sales/history?page=2&size=20',
    );
  });

  it('only forwards paths under /api: the proxy is not an open door to the backend', () => {
    expect(() => upstreamUrl(BACKEND, '/actuator/env', '')).toThrow();
    expect(() => upstreamUrl(BACKEND, '/apixyz', '')).toThrow();
  });

  it('cannot be redirected to another host', () => {
    expect(() => upstreamUrl(BACKEND, '//evil.example/api/x', '')).toThrow();
  });
});

describe('clientIpFrom', () => {
  it('takes the first address of X-Forwarded-For, the one Vercel writes for the browser', () => {
    expect(clientIpFrom(new Headers({ 'x-forwarded-for': '181.45.12.7, 10.0.0.1' }))).toBe('181.45.12.7');
  });

  it('falls back to X-Real-IP', () => {
    expect(clientIpFrom(new Headers({ 'x-real-ip': '2800:810:4a3::1' }))).toBe('2800:810:4a3::1');
  });

  it('returns nothing when no proxy reported an address', () => {
    expect(clientIpFrom(new Headers())).toBeUndefined();
    expect(clientIpFrom(new Headers({ 'x-forwarded-for': ' ' }))).toBeUndefined();
  });
});

describe('upstreamRequestHeaders', () => {
  const browser = new Headers({
    accept: 'application/json',
    authorization: 'Bearer abc',
    'content-type': 'application/json',
    cookie: 'theme=dark',
    host: 'kiosco-la-madrina.vercel.app',
    origin: 'https://kiosco-la-madrina.vercel.app',
    'x-forwarded-for': '181.45.12.7',
    'x-vercel-id': 'gru1::abc',
    // Un navegador malicioso intenta hacerse pasar por el proxy.
    [SECRET_HEADER]: 'adivinando',
    [CLIENT_IP_HEADER]: '1.2.3.4',
  });

  it('forwards what the API needs: session, body type and cookies', () => {
    const sent = upstreamRequestHeaders(browser, {});
    expect(sent.get('authorization')).toBe('Bearer abc');
    expect(sent.get('content-type')).toBe('application/json');
    expect(sent.get('accept')).toBe('application/json');
    expect(sent.get('cookie')).toBe('theme=dark');
  });

  it('drops everything else (allowlist), including the Origin of a server-to-server call', () => {
    const sent = upstreamRequestHeaders(browser, {});
    for (const name of ['host', 'origin', 'x-forwarded-for', 'x-vercel-id']) {
      expect(sent.has(name), name).toBe(false);
    }
  });

  it('never trusts the proxy headers sent by the browser', () => {
    const sent = upstreamRequestHeaders(browser, {});
    expect(sent.has(SECRET_HEADER)).toBe(false);
    expect(sent.has(CLIENT_IP_HEADER)).toBe(false);
  });

  it('adds its own secret and the client IP', () => {
    const sent = upstreamRequestHeaders(browser, { secret: 'el-secreto', clientIp: '181.45.12.7' });
    expect(sent.get(SECRET_HEADER)).toBe('el-secreto');
    expect(sent.get(CLIENT_IP_HEADER)).toBe('181.45.12.7');
  });
});

describe('downstreamResponseHeaders', () => {
  it('passes the API headers through, every Set-Cookie included', () => {
    const upstream = new Headers({ 'content-type': 'application/json', 'retry-after': '60' });
    upstream.append('set-cookie', 'a=1; HttpOnly');
    upstream.append('set-cookie', 'b=2; HttpOnly');

    const sent = downstreamResponseHeaders(upstream);
    expect(sent.get('content-type')).toBe('application/json');
    expect(sent.get('retry-after')).toBe('60');
    expect(sent.getSetCookie()).toEqual(['a=1; HttpOnly', 'b=2; HttpOnly']);
  });

  it('drops the encoding and length: fetch already decompressed the body', () => {
    const sent = downstreamResponseHeaders(
      new Headers({ 'content-encoding': 'gzip', 'content-length': '120', 'transfer-encoding': 'chunked' }),
    );
    expect([...sent.keys()]).toEqual([]);
  });
});
