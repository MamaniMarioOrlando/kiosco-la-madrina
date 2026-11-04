import { toast } from 'sonner';

// Un único id: si el dashboard y "Nueva Venta" avisan a la vez, sonner muestra un solo toast.
const LOW_STOCK_TOAST = 'low-stock-alert';

/**
 * Avisa cuántos productos necesitan reposición. Es un `warning` y no un `error`:
 * no falló ninguna operación, es información para que el kiosquero actúe.
 */
export function notifyLowStock(count: number): void {
  if (count <= 0) return;

  toast.warning('Atención: Stock Bajo', {
    id: LOW_STOCK_TOAST,
    description: count === 1
      ? 'Hay 1 producto que necesita reposición.'
      : `Hay ${count} productos que necesitan reposición.`,
    duration: 6000,
  });
}
