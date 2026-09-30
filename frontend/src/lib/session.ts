'use client';

import { useSyncExternalStore } from 'react';

/**
 * Única fuente de verdad del usuario logueado en el navegador.
 *
 * Antes cada componente leía localStorage por su cuenta y solo al montarse, así que un cambio
 * (p. ej. el avatar) no se veía en el resto de la pantalla sin recargar. Con este store, quien
 * lo modifica notifica y todos los componentes suscritos se actualizan al instante.
 */

export interface SessionUser {
    id: number;
    username: string;
    roles: string[];
    avatarUrl?: string | null;
}

const TOKEN_KEY = 'token';
const USER_KEY = 'user';
const CHANGE_EVENT = 'session-change';

// useSyncExternalStore exige que el snapshot sea la misma referencia mientras no cambie.
let cachedRaw: string | null = null;
let cachedUser: SessionUser | null = null;

function readUser(): SessionUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (raw === cachedRaw) return cachedUser;

    cachedRaw = raw;
    try {
        cachedUser = raw ? (JSON.parse(raw) as SessionUser) : null;
    } catch {
        cachedUser = null;
    }
    return cachedUser;
}

function notify() {
    window.dispatchEvent(new Event(CHANGE_EVENT));
}

function subscribe(onChange: () => void) {
    window.addEventListener(CHANGE_EVENT, onChange);
    // 'storage' sincroniza otras pestañas del mismo navegador (p. ej. un logout).
    window.addEventListener('storage', onChange);
    return () => {
        window.removeEventListener(CHANGE_EVENT, onChange);
        window.removeEventListener('storage', onChange);
    };
}

export function startSession(token: string, user: SessionUser) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    notify();
}

export function updateSessionUser(changes: Partial<SessionUser>) {
    const current = readUser();
    if (!current) return;
    localStorage.setItem(USER_KEY, JSON.stringify({ ...current, ...changes }));
    notify();
}

export function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    notify();
}

export function getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
}

export function isAdmin(user: SessionUser | null): boolean {
    return user?.roles?.some((role) => role.toUpperCase() === 'ADMIN') ?? false;
}

/**
 * Usuario actual, reactivo. En el servidor (y durante la hidratación) devuelve null:
 * los componentes deben mostrar un placeholder neutro en ese caso, no datos inventados.
 */
export function useCurrentUser(): SessionUser | null {
    return useSyncExternalStore(subscribe, readUser, () => null);
}
