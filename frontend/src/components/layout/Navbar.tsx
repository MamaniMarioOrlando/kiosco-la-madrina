'use client';

import { Button } from '@/components/ui/button';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { LogOut, Sun, Moon } from 'lucide-react';
import { useTheme } from '@/components/providers/ThemeProvider';
import { cn } from '@/lib/utils';

export function Navbar() {
    const router = useRouter();
    const [username, setUsername] = useState<string>('');
    const [role, setRole] = useState<string>('');
    const { theme, toggleTheme } = useTheme();

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            try {
                const parsed = JSON.parse(userData);
                setUsername(parsed.username || 'Usuario');
                if (parsed.roles && parsed.roles.length > 0) {
                    setRole(parsed.roles[0].replace('ROLE_', ''));
                }
            } catch (e) {
                setUsername('Usuario');
            }
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        router.push('/login');
    };

    const isRoleAdmin = role.toLowerCase().includes('admin');

    return (
        <header className="flex h-16 items-center justify-end gap-2 border-b bg-white dark:bg-slate-900 px-6 shadow-sm border-slate-200 dark:border-slate-800">
            <Button
                variant="ghost"
                size="icon"
                onClick={toggleTheme}
                className="text-slate-500 dark:text-slate-400 hover:text-orange-500 dark:hover:text-orange-400 hover:bg-orange-50 dark:hover:bg-orange-950/20 rounded-full transition-colors"
            >
                {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
            </Button>

            <div className="h-4 w-[1px] bg-slate-200 dark:bg-slate-800 mx-2" />

            <div className="flex items-center gap-3 mr-2">
                <div className="text-right">
                    <div className="text-sm font-bold text-slate-900 dark:text-white leading-none capitalize">
                        {username}
                    </div>
                </div>
                <div className={cn(
                    "px-2 py-0.5 rounded text-[10px] font-black uppercase tracking-tighter border",
                    isRoleAdmin
                        ? "bg-orange-50 border-orange-200 text-orange-600 dark:bg-orange-900/20 dark:border-orange-900/50 dark:text-orange-400"
                        : "bg-blue-50 border-blue-200 text-blue-600 dark:bg-blue-900/20 dark:border-blue-900/50 dark:text-blue-400"
                )}>
                    {role || 'User'}
                </div>
            </div>

            <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="text-slate-500 hover:text-red-600 dark:hover:text-red-400"
            >
                <LogOut className="mr-2 h-4 w-4" />
                Salir
            </Button>
        </header>
    );
}
