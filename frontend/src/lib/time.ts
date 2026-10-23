/**
 * Zona horaria del negocio. Las ventas se muestran y se agrupan por día en hora de Argentina,
 * sin importar la zona configurada en la computadora que abre la app.
 */
const KIOSK_TIME_ZONE = 'America/Argentina/Buenos_Aires';

// en-CA formatea las fechas como YYYY-MM-DD.
const kioskDateFormat = new Intl.DateTimeFormat('en-CA', { timeZone: KIOSK_TIME_ZONE });

/** Fecha de hoy en el kiosco, en formato YYYY-MM-DD (el que espera la API). */
export function todayInKiosk(): string {
    return kioskDateFormat.format(new Date());
}

/** Si un instante de la API cae en el día de hoy del kiosco (p. ej. para saber si una venta se puede anular). */
export function isTodayInKiosk(isoInstant: string): boolean {
    return kioskDateFormat.format(new Date(isoInstant)) === todayInKiosk();
}

// Formato explícito en 24 h: el formato por defecto de es-AR usa 12 h sin "a. m./p. m.",
// así que las 23:53 se veían como "11:53".
const kioskDateTimeFormat = new Intl.DateTimeFormat('es-AR', {
    timeZone: KIOSK_TIME_ZONE,
    day: 'numeric',
    month: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23',
});

/** Fecha y hora legibles en hora del kiosco, a partir de un instante ISO de la API (p. ej. "2026-10-22T02:30:00Z"). */
export function formatKioskDateTime(isoInstant: string): string {
    return kioskDateTimeFormat.format(new Date(isoInstant));
}
