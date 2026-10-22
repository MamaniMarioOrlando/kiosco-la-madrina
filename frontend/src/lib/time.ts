/**
 * Zona horaria del negocio. Las ventas se muestran y se agrupan por día en hora de Argentina,
 * sin importar la zona configurada en la computadora que abre la app.
 */
const KIOSK_TIME_ZONE = 'America/Argentina/Buenos_Aires';

/** Fecha de hoy en el kiosco, en formato YYYY-MM-DD (el que espera la API). */
export function todayInKiosk(): string {
    // en-CA formatea las fechas como YYYY-MM-DD.
    return new Intl.DateTimeFormat('en-CA', { timeZone: KIOSK_TIME_ZONE }).format(new Date());
}

/** Fecha y hora legibles en hora del kiosco, a partir de un instante ISO de la API (p. ej. "2026-10-22T02:30:00Z"). */
export function formatKioskDateTime(isoInstant: string): string {
    return new Date(isoInstant).toLocaleString('es-AR', { timeZone: KIOSK_TIME_ZONE });
}
