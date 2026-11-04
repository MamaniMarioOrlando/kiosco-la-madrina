'use client';

import { useState, type ReactNode } from 'react';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { cn } from '@/lib/utils';

interface ConfirmDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    title: string;
    description: ReactNode;
    confirmLabel: string;
    /** Pinta el botón de rojo: la acción saca algo de circulación. */
    destructive?: boolean;
    onConfirm: () => void;
}

/** Reemplazo de `window.confirm()`: no bloquea la página y respeta el tema. */
export function ConfirmDialog({
    open,
    onOpenChange,
    title,
    description,
    confirmLabel,
    destructive = false,
    onConfirm,
}: ConfirmDialogProps) {
    return (
        <AlertDialog open={open} onOpenChange={onOpenChange}>
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle>{title}</AlertDialogTitle>
                    <AlertDialogDescription>{description}</AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>Cancelar</AlertDialogCancel>
                    <AlertDialogAction
                        onClick={onConfirm}
                        className={cn(destructive && 'bg-red-600 text-white hover:bg-red-700 dark:bg-red-600 dark:hover:bg-red-700')}
                    >
                        {confirmLabel}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
}

/**
 * Recuerda sobre qué elemento se pidió confirmación.
 * `open` y `target` van separados a propósito: al cerrar, el diálogo hace una
 * animación de salida, y si `target` se borrara en ese momento el texto
 * quedaría vacío ("¿Desactivar ""?") durante la animación.
 */
export function useConfirmTarget<T>() {
    const [target, setTarget] = useState<T | null>(null);
    const [open, setOpen] = useState(false);

    const ask = (item: T) => {
        setTarget(item);
        setOpen(true);
    };

    return { target, ask, dialogProps: { open, onOpenChange: setOpen } };
}
