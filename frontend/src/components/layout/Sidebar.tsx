'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';
import {
    LayoutDashboard,
    ShoppingBasket,
    Tags,
    FileText,
    Settings,
    Menu,
    X,
    ShoppingCart,
    History as HistoryIcon
} from 'lucide-react';
import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import api from '@/lib/api';

const sidebarItems = [
    { icon: LayoutDashboard, label: 'Dashboard', href: '/' },
    { icon: ShoppingCart, label: 'Nueva Venta', href: '/sales' },
    { icon: HistoryIcon, label: 'Historial', href: '/sales/history' },
    { icon: Tags, label: 'Categorías', href: '/categories', adminOnly: true },
    { icon: ShoppingBasket, label: 'Productos', href: '/products' },
    { icon: Settings, label: 'Configuración', href: '/settings' },
];

export function Sidebar({ className }: { className?: string }) {
    const pathname = usePathname();
    const [isOpen, setIsOpen] = useState(false);

    const [user, setUser] = useState<{ roles: string[] } | null>(null);
    const [hasLowStock, setHasLowStock] = useState(false);

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            setUser(JSON.parse(userData));
        }
        checkLowStock();

        // Refresh status every 30 seconds
        const interval = setInterval(checkLowStock, 30000);
        return () => clearInterval(interval);
    }, [pathname]);

    const checkLowStock = async () => {
        try {
            const response = await api.get('/products');
            const low = response.data.some((p: any) => p.stockQuantity < 5);
            setHasLowStock(low);
        } catch (e) {
            console.error(e);
        }
    };

    const isAdmin = user?.roles?.some(r => r.toLowerCase().includes('admin'));

    return (
        <>
            {/* Mobile Toggle */}
            <div className="lg:hidden fixed top-4 left-4 z-50">
                <Button variant="outline" size="icon" onClick={() => setIsOpen(!isOpen)}>
                    {isOpen ? <X /> : <Menu />}
                </Button>
            </div>

            {/* Sidebar Container */}
            <aside
                className={cn(
                    "fixed inset-y-0 left-0 z-40 w-64 transform bg-slate-900 text-white transition-transform duration-200 ease-in-out lg:static lg:translate-x-0",
                    isOpen ? "translate-x-0" : "-translate-x-full",
                    className
                )}
            >
                <div className="flex h-16 items-center justify-center border-b border-slate-800">
                    <h1 className="text-xl font-bold text-orange-500">Kiosco La Madrina</h1>
                </div>

                <nav className="mt-6 px-4 space-y-2">
                    {sidebarItems.map((item) => {
                        if (item.adminOnly && !isAdmin) return null;

                        const Icon = item.icon;
                        const isActive = pathname === item.href;
                        return (
                            <Link
                                key={item.href}
                                href={item.href}
                                onClick={() => setIsOpen(false)}
                                className={cn(
                                    "flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium transition-colors",
                                    isActive
                                        ? "bg-orange-600 text-white"
                                        : "text-slate-400 hover:bg-slate-800 hover:text-white"
                                )}
                            >
                                <div className="relative">
                                    <Icon className="h-5 w-5" />
                                    {hasLowStock && (item.label === 'Dashboard' || item.label === 'Productos') && (
                                        <span className="absolute -top-1 -right-1 flex h-2.5 w-2.5">
                                            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                                            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-red-600"></span>
                                        </span>
                                    )}
                                </div>
                                {item.label}
                            </Link>
                        );
                    })}
                </nav>
            </aside>

            {/* Overlay for mobile */}
            {isOpen && (
                <div
                    className="fixed inset-0 z-30 bg-black/50 lg:hidden"
                    onClick={() => setIsOpen(false)}
                />
            )}
        </>
    );
}
