import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Se crea una sola vez: armar un Intl.NumberFormat es caro y formatCurrency se llama en cada fila.
const pesos = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' });

export function formatCurrency(amount: number): string {
  return pesos.format(amount);
}
