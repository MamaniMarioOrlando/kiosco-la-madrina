'use client';

import { useState } from 'react';
import { saleService } from '@/services/saleService';
import { apiErrorMessage } from '@/lib/errors';
import { formatKioskDateTime, isTodayInKiosk } from '@/lib/time';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Ban } from 'lucide-react';
import type { Sale } from '@/types/sale';

/**
 * Lo único que este componente necesita de una venta (principio de segregación de interfaces):
 * acepta cualquier objeto con estos campos, no exige la venta completa.
 */
export type VoidableSale = Pick<Sale, 'id' | 'dateTime' | 'voidedAt' | 'voidedBy' | 'voidReason'>;

interface VoidSaleProps {
    sale: VoidableSale;
    canVoid: boolean;
    /** Recibe la venta completa y actualizada que devuelve la API. */
    onVoided: (sale: Sale) => void;
}

/**
 * Anulación de una venta dentro de su detalle. Si ya está anulada, muestra quién, cuándo y por qué.
 * Si no, y el usuario es admin y la venta es de hoy, permite anularla con un motivo obligatorio.
 */
export function VoidSale({ sale, canVoid, onVoided }: VoidSaleProps) {
    const [confirming, setConfirming] = useState(false);
    const [reason, setReason] = useState('');
    const [saving, setSaving] = useState(false);

    if (sale.voidedAt) {
        return (
            <div className="mt-4 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700 dark:border-red-900/50 dark:bg-red-950/30 dark:text-red-400">
                <div className="font-bold">Venta anulada</div>
                <div>
                    {formatKioskDateTime(sale.voidedAt)} por {sale.voidedBy}: {sale.voidReason}
                </div>
                <div className="mt-1 text-xs opacity-80">No suma en la caja y su mercadería volvió al stock.</div>
            </div>
        );
    }

    // Regla de negocio: solo ventas del día. Un día cuya caja ya se cerró no cambia.
    if (!canVoid || !isTodayInKiosk(sale.dateTime)) return null;

    const submit = async () => {
        setSaving(true);
        try {
            const voided = await saleService.voidSale(sale.id, reason.trim());
            toast.success(`Venta #${sale.id} anulada. La mercadería volvió al stock.`);
            onVoided(voided);
        } catch (err) {
            toast.error(apiErrorMessage(err, 'No se pudo anular la venta'));
        } finally {
            setSaving(false);
        }
    };

    if (!confirming) {
        return (
            <div className="mt-4 flex justify-end">
                <Button variant="outline" onClick={() => setConfirming(true)} className="text-red-600 dark:text-red-400">
                    <Ban className="mr-2 h-4 w-4" /> Anular venta
                </Button>
            </div>
        );
    }

    return (
        <div className="mt-4 space-y-3 rounded-xl border border-red-200 p-4 dark:border-red-900/50">
            <label htmlFor="void-reason" className="block text-sm font-medium text-slate-700 dark:text-slate-300">
                Motivo de la anulación
            </label>
            <input
                id="void-reason"
                autoFocus
                maxLength={255}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="Ej.: cobré de más, el cliente se arrepintió"
                className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-950 dark:text-white"
            />
            <div className="flex justify-end gap-2">
                <Button variant="outline" disabled={saving} onClick={() => { setConfirming(false); setReason(''); }}>
                    Cancelar
                </Button>
                <Button
                    disabled={saving || reason.trim() === ''}
                    onClick={submit}
                    className="bg-red-600 text-white hover:bg-red-700"
                >
                    {saving ? 'Anulando...' : 'Confirmar anulación'}
                </Button>
            </div>
        </div>
    );
}
