import type { LucideIcon } from 'lucide-react';

interface PageHeaderProps {
    title: string;
    description?: string;
    /** Obligatorio: el mismo ícono que la sección tiene en el Sidebar, para que todas las pantallas se vean iguales. */
    icon: LucideIcon;
    /** Acciones a la derecha del título, p. ej. el botón "Nuevo Producto". */
    children?: React.ReactNode;
}

/**
 * Encabezado común de las pantallas del panel. Antes cada pantalla copiaba este bloque y una de
 * las copias (Historial) perdió los colores del modo oscuro: el título quedaba casi invisible.
 */
export function PageHeader({ title, description, icon: Icon, children }: PageHeaderProps) {
    return (
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div>
                <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
                    <Icon className="h-6 w-6 text-orange-600" />
                    {title}
                </h2>
                {description && <p className="text-slate-500 dark:text-slate-400">{description}</p>}
            </div>
            {children}
        </div>
    );
}
