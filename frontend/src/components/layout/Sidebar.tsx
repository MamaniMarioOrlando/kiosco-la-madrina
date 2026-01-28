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

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            setUser(JSON.parse(userData));
        }
    }, []);

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
                                <Icon className="h-5 w-5" />
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
